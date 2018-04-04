package com.six.node_manager.role.looking;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.rpc.annotation.DoveService;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeResourceCollect;
import com.six.node_manager.NodeStatus;
import com.six.node_manager.RemoteAdapter;
import com.six.node_manager.core.ClusterNodes;
import com.six.node_manager.role.AbstractNodeRole;
import com.six.node_manager.role.NodeRole;
import com.six.node_manager.role.master.MasterNodeRole;
import com.six.node_manager.role.slave.SlaveNodeRole;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2018年1月19日 下午7:38:11 类说明
 */

public class LookingNodeRole extends AbstractNodeRole implements NodeRole {

	private static Logger log = LoggerFactory.getLogger(LookingNodeRole.class);
	private LinkedBlockingQueue<MasterProposal> revMasterProposalQueue = new LinkedBlockingQueue<>();
	private MasterProposal currentMasterProposal;
	private AtomicInteger logicClock = new AtomicInteger(0);
	private final static int maxNotificationInterval = 60000;
	private final static int finalizeWait = 200;
	private final static int IGNOREVALUE = -1;

	public LookingNodeRole(RemoteAdapter remoteAdapter, NodeResourceCollect nodeResourceCollect, NodeInfo master,
			ClusterNodes clusterNodes, long workInterval, int allowHeartbeatErrCount) {
		super(remoteAdapter, nodeResourceCollect, master, clusterNodes, workInterval, allowHeartbeatErrCount);
		getRemoteAdapter().registerNodeRpcProtocol(getExecutorService(), new LookingNodeRoleServiceImpl());
	}

	@Override
	public NodeRole work() {
		NodeRole staticNodeRole = null;
		NodeInfo masterNodeInfo = election();
		if (getNode().getName().equals(masterNodeInfo.getName())) {
			getNode().master();
			staticNodeRole = new MasterNodeRole(getRemoteAdapter(), getNodeResourceCollect(), masterNodeInfo,
					getClusterNodes(), getHeartbeatInterval(), getAllowHeartbeatErrCount());
		} else {
			getNode().slave();
			staticNodeRole = new SlaveNodeRole(getRemoteAdapter(), getNodeResourceCollect(), masterNodeInfo,
					getClusterNodes(), getHeartbeatInterval(), getAllowHeartbeatErrCount());
		}
		return staticNodeRole;
	}

	private NodeInfo election() {
		NodeInfo won = null;
		synchronized (this) {
			logicClock.incrementAndGet();
			this.currentMasterProposal = newMasterProposal(getNode().nodeInfo());
		}
		sendMasterProposal();
		Map<String, NodeInfo> masterProposalScoreMap = new HashMap<>();
		HashMap<String, NodeInfo> outofelection = new HashMap<String, NodeInfo>();
		MasterProposal masterProposal = null;
		long notTimeout = finalizeWait;
		while (null == won) {
			try {
				masterProposal = revMasterProposalQueue.poll(notTimeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
			}
			if (null == masterProposal) {
				sendMasterProposal();
				long tmpTimeOut = notTimeout * 2;
				notTimeout = (tmpTimeOut < maxNotificationInterval ? tmpTimeOut : maxNotificationInterval);
				log.info("send proposal time out: " + notTimeout);
			} else if (null == masterProposal.getNode()) {
				log.warn("Ignoring proposal and the proposal's master is null");
			} else if (isMember(masterProposal.getNode())) {
				switch (masterProposal.getNode().getState()) {
				case LOOKING:
					if (masterProposal.getLogicClock() < logicClock.get()) {
						log.warn("the MasterProposal{" + masterProposal.toString() + "}'s logicClock["
								+ masterProposal.getLogicClock() + "]<currentLogicClock["
								+ currentMasterProposal.getLogicClock() + "]");
					} else if (masterProposal.getLogicClock() > logicClock.get()) {
						logicClock.set(masterProposal.getLogicClock());
						masterProposalScoreMap.clear();
						if (isWon(masterProposal, currentMasterProposal)) {
							this.currentMasterProposal = newMasterProposal(masterProposal.getNode());
						} else {
							this.currentMasterProposal = newMasterProposal(getNode().nodeInfo());
						}
						sendMasterProposal();
					} else if (isWon(masterProposal, currentMasterProposal)) {
						this.currentMasterProposal = newMasterProposal(masterProposal.getNode());
						sendMasterProposal();
					}
					masterProposalScoreMap.put(masterProposal.getProposer(), masterProposal.getNode());
					if (isWon(masterProposalScoreMap, currentMasterProposal)) {
						try {
							while ((masterProposal = revMasterProposalQueue.poll(finalizeWait,
									TimeUnit.MILLISECONDS)) != null) {
								if (isWon(masterProposal, currentMasterProposal)) {
									revMasterProposalQueue.put(masterProposal);
									break;
								}
							}
						} catch (InterruptedException e) {

						}
						if (masterProposal == null) {
							won = currentMasterProposal.getNode();
							revMasterProposalQueue.clear();
							break;
						}
					}
					break;
				case SLAVE:
				case MASTER:
					if (masterProposal.getLogicClock() == logicClock.get()) {
						masterProposalScoreMap.put(masterProposal.getProposer(), masterProposal.getNode());
						if (isWon(masterProposalScoreMap, masterProposal)
								&& checkMaster(outofelection, masterProposal, masterProposal.getLogicClock())) {
							won = masterProposal.getNode();
							revMasterProposalQueue.clear();
							return won;
						}
					}
					outofelection.put(masterProposal.getProposer(), masterProposal.getNode());
					if (isWon(outofelection, masterProposal)
							&& checkMaster(outofelection, masterProposal, IGNOREVALUE)) {
						synchronized (this) {
							logicClock.set(masterProposal.getLogicClock());
						}
						won = masterProposal.getNode();
						revMasterProposalQueue.clear();
						return won;
					}
					break;
				default:
					// Ignoring
					break;
				}
			} else {
				log.warn("Ignoring proposal from non-cluster member:" + masterProposal.getNode().getName());
			}
		}
		return won;
	}

	protected boolean isWon(Map<String, NodeInfo> masterProposalScoreMap, MasterProposal masterProposal) {
		if (!isMember(masterProposal.getNode())) {
			return false;
		} else {
			int totalVotes = 0;
			for (Map.Entry<String, NodeInfo> entry : masterProposalScoreMap.entrySet()) {
				if (masterProposal.getNode().equals(entry.getValue())) {
					totalVotes++;
				}
			}
			return getClusterNodes().isMoreThanHalfProcess(totalVotes);
		}
	}

	protected boolean isWon(MasterProposal masterProposal1, MasterProposal masterProposal2) {
		if (masterProposal1.getNode().getVersion() > masterProposal2.getNode().getVersion()) {
			return true;
		} else if (masterProposal1.getNode().getVersion() == masterProposal2.getNode().getVersion()
				&& masterProposal1.getNode().getName().compareTo(masterProposal2.getNode().getName()) > 0) {
			return true;
		}
		return false;
	}

	private boolean checkMaster(Map<String, NodeInfo> outofelection, MasterProposal masterProposal,
			int electionLogicClock) {
		boolean predicate = true;
		if (!getNode().getName().equals(masterProposal.getNode().getName())) {
			if (outofelection.get(masterProposal.getNode().getName()) == null)
				predicate = false;
			else if (outofelection.get(masterProposal.getNode().getName()).getState() != NodeStatus.MASTER)
				predicate = false;
		} else if (logicClock.get() != electionLogicClock) {
			predicate = false;
		}
		return predicate;
	}

	private void sendMasterProposal() {
		getClusterNodes().forEachNeedDiscoveryNodeInfos((nodeName, nodeInfo) -> {
			try {
				LookingNodeRoleService rpcNodeDiscoveryProtocol = getRemoteAdapter().lookupNodeRpcProtocol(nodeInfo,
						LookingNodeRoleService.class, result -> {
							if (result.isSuccessed()) {
								log.info("rpc rpcNodeDiscoveryProtocol.sendMasterProposal[" + nodeInfo + "]successed");
								if (null != result.getResult()) {
									revMasterProposalQueue.add((MasterProposal) result.getResult());
								}
							} else {
								log.warn("rpc rpcNodeDiscoveryProtocol.sendMasterProposal[" + nodeInfo + "] failed");
							}
						});
				rpcNodeDiscoveryProtocol.sendMasterProposal(currentMasterProposal);
			} catch (Exception e) {
				log.error("rpc rpcNodeDiscoveryProtocol.sendMasterProposal exception", e);
			}
		});
	}

	private MasterProposal newMasterProposal(NodeInfo node) {
		NodeInfo copyNode = node.copy();
		copyNode.setState(NodeStatus.LOOKING);
		MasterProposal masterProposal = new MasterProposal();
		masterProposal.setProposer(getNode().getName());
		masterProposal.setNode(copyNode);
		masterProposal.setLogicClock(logicClock.get());
		return masterProposal;
	}

	@Override
	public void leave() {

	}

	@DoveService(protocol = LookingNodeRoleService.class)
	public class LookingNodeRoleServiceImpl implements LookingNodeRoleService {

		@Override
		public MasterProposal sendMasterProposal(MasterProposal masterProposal) {
			if (null != masterProposal) {
				if (NodeStatus.LOOKING == getNode().getNodeState()) {
					revMasterProposalQueue.add(masterProposal);
				} else {
					return currentMasterProposal;
				}
			}
			return null;
		}
	}
}
