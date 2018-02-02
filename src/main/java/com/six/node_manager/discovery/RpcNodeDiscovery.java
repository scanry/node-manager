package com.six.node_manager.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.rpc.annotation.DoveService;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.NodeState;
import com.six.node_manager.core.ClusterNodes;
import com.six.node_manager.core.SpiExtension;
import com.six.node_manager.discovery.protocol.RpcNodeDiscoveryProtocol;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class RpcNodeDiscovery extends AbstractNodeDiscovery {

	private static Logger log = LoggerFactory.getLogger(RpcNodeDiscovery.class);
	private NodeProtocolManager nodeProtocolManager = SpiExtension.getInstance().find(NodeProtocolManager.class);
	private LinkedBlockingQueue<MasterProposal> revMasterProposalQueue = new LinkedBlockingQueue<>();
	private MasterProposal currentMasterProposal;
	private AtomicInteger logicClock = new AtomicInteger(0);
	private final static int maxNotificationInterval = 60000;
	private final static int finalizeWait = 200;
	private final static int IGNOREVALUE = -1;

	public RpcNodeDiscovery(ClusterNodes clusterNodes, Map<String, NodeInfo> needDiscoveryNodeInfos,
			long heartbeatInterval, int allowHeartbeatErrCount) {
		super(clusterNodes, heartbeatInterval, allowHeartbeatErrCount);
		nodeProtocolManager.registerNodeRpcProtocol(new RpcNodeDiscoveryProtocolImpl());
	}

	@Override
	protected NodeInfo doElection() {
		NodeInfo won = null;
		synchronized (this) {
			logicClock.incrementAndGet();
			this.currentMasterProposal = newMasterProposal(getLocalNodeInfo());
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
							this.currentMasterProposal = newMasterProposal(getLocalNodeInfo());
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
							return won;
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

	protected boolean isMember(NodeInfo node) {
		return getClusterNodes().isMember(node.getName());
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
		if (!getLocalNodeName().equals(masterProposal.getNode().getName())) {
			if (outofelection.get(masterProposal.getNode().getName()) == null)
				predicate = false;
			else if (outofelection.get(masterProposal.getNode().getName()).getState() != NodeState.MASTER)
				predicate = false;
		} else if (logicClock.get() != electionLogicClock) {
			predicate = false;
		}
		return predicate;
	}

	private void sendMasterProposal() {
		getClusterNodes().forEachNeedDiscoveryNodeInfos((nodeName, nodeInfo) -> {
			try {
				RpcNodeDiscoveryProtocol rpcNodeDiscoveryProtocol = nodeProtocolManager.lookupNodeRpcProtocol(nodeInfo,
						RpcNodeDiscoveryProtocol.class, result -> {
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
		copyNode.setState(NodeState.LOOKING);
		MasterProposal masterProposal = new MasterProposal();
		masterProposal.setProposer(getLocalNodeName());
		masterProposal.setNode(copyNode);
		masterProposal.setLogicClock(logicClock.get());
		return masterProposal;
	}

	@DoveService(protocol=RpcNodeDiscoveryProtocol.class)
	public class RpcNodeDiscoveryProtocolImpl implements RpcNodeDiscoveryProtocol {

		@Override
		public MasterProposal sendMasterProposal(MasterProposal masterProposal) {
			if (null != masterProposal) {
				if (NodeState.LOOKING == getNodeState()) {
					revMasterProposalQueue.add(masterProposal);
				} else {
					return currentMasterProposal;
				}
			}
			return null;
		}
	}
}
