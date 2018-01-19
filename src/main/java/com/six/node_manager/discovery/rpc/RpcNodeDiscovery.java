package com.six.node_manager.discovery.rpc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeEventManager;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.NodeState;
import com.six.node_manager.discovery.AbstractNodeDiscovery;
import com.six.node_manager.discovery.SlaveNodeDiscoveryProtocol;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class RpcNodeDiscovery extends AbstractNodeDiscovery {

	private static Logger log = LoggerFactory.getLogger(RpcNodeDiscovery.class);

	private LinkedBlockingQueue<MasterProposal> masterProposalQueue = new LinkedBlockingQueue<>();
	private Map<String, NodeInfo> needDiscoveryNodeInfos;
	private MasterProposal currentMasterProposal;
	private AtomicInteger logicClock = new AtomicInteger(0);
	private final static int maxNotificationInterval = 60000;
	private final static int finalizeWait = 200;
	private final static int IGNOREVALUE = -1;

	public RpcNodeDiscovery(NodeInfo localNodeInfo, Map<String, NodeInfo> needDiscoveryNodeInfos,
			NodeProtocolManager nodeProtocolManager, NodeEventManager nodeEventManager) {
		super(localNodeInfo, nodeProtocolManager, nodeEventManager);
		if (null != needDiscoveryNodeInfos) {
			this.needDiscoveryNodeInfos = new HashMap<>(needDiscoveryNodeInfos.size());
			for (Map.Entry<String, NodeInfo> entry : needDiscoveryNodeInfos.entrySet()) {
				this.needDiscoveryNodeInfos.put(entry.getKey(), entry.getValue().copy());
			}
		} else {
			this.needDiscoveryNodeInfos = Collections.emptyMap();
		}
		nodeProtocolManager.registerNodeRpcProtocol(RpcNodeDiscoveryProtocol.class, new RpcNodeDiscoveryProtocolImpl());
	}

	public RpcNodeDiscovery(NodeInfo localNodeInfo, Map<String, NodeInfo> needDiscoveryNodeInfos,
			NodeProtocolManager nodeProtocolManager, NodeEventManager nodeEventManager, long heartbeatInterval,
			int allowHeartbeatErrCount) {
		super(localNodeInfo, nodeProtocolManager, nodeEventManager, heartbeatInterval, allowHeartbeatErrCount);
		if (null != needDiscoveryNodeInfos) {
			this.needDiscoveryNodeInfos = new HashMap<>(needDiscoveryNodeInfos.size());
			for (Map.Entry<String, NodeInfo> entry : needDiscoveryNodeInfos.entrySet()) {
				this.needDiscoveryNodeInfos.put(entry.getKey(), entry.getValue().copy());
			}
		} else {
			this.needDiscoveryNodeInfos = Collections.emptyMap();
		}
		nodeProtocolManager.registerNodeRpcProtocol(RpcNodeDiscoveryProtocol.class, new RpcNodeDiscoveryProtocolImpl());
	}

	private int getWonSize() {
		return needDiscoveryNodeInfos.size() / 2 + 1;
	}

	@Override
	protected NodeInfo askWhoIsMaster() {
		NodeInfo askWhoIsMaster = null;
		SlaveNodeDiscoveryProtocol slaveNodeDiscoveryProtocol = null;
		for (NodeInfo nodeInfo : needDiscoveryNodeInfos.values()) {
			try {
				slaveNodeDiscoveryProtocol = getNodeProtocolManager().lookupNodeRpcProtocol(nodeInfo,
						SlaveNodeDiscoveryProtocol.class);
				askWhoIsMaster = slaveNodeDiscoveryProtocol.getMasterNode();
				if (null != askWhoIsMaster) {
					return askWhoIsMaster;
				}
			} catch (Exception e) {
				log.error("rpc rpcNodeDiscoveryProtocol.askWhoIsMaster exception", e);
			}
		}
		return askWhoIsMaster;
	}

	@Override
	protected NodeInfo doElection() {
		NodeInfo won = null;
		if (null == needDiscoveryNodeInfos || needDiscoveryNodeInfos.isEmpty()) {
			won = getLocalNodeInfo();
		} else {
			synchronized (this) {
				logicClock.incrementAndGet();
				this.currentMasterProposal = newMasterProposal(getLocalNodeInfo());
			}
			sendMasterProposal();
			Map<String, NodeInfo> masterProposalScoreMap = new HashMap<>();
			HashMap<String, NodeInfo> outofelection = new HashMap<String, NodeInfo>();
			int winSize = getWonSize();
			MasterProposal masterProposal = null;
			long notTimeout = finalizeWait;
			while (null == won) {
				try {
					masterProposal = masterProposalQueue.poll(notTimeout, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
				}
				if (null == masterProposal) {
					if (allPingSuccessed()) {
						sendMasterProposal();
					}
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
						if (isWon(masterProposalScoreMap, currentMasterProposal, winSize)) {
							try {
								while ((masterProposal = masterProposalQueue.poll(finalizeWait,
										TimeUnit.MILLISECONDS)) != null) {
									if (isWon(masterProposal, currentMasterProposal)) {
										masterProposalQueue.put(masterProposal);
										break;
									}
								}
							} catch (InterruptedException e) {

							}
							if (masterProposal == null) {
								won = currentMasterProposal.getNode();
								masterProposalQueue.clear();
								return won;
							}
						}

						break;
					case SLAVE:
					case MASTER:
						if (masterProposal.getLogicClock() == logicClock.get()) {
							masterProposalScoreMap.put(masterProposal.getProposer(), masterProposal.getNode());
							if (isWon(masterProposalScoreMap, masterProposal, winSize)
									&& checkMaster(outofelection, masterProposal, masterProposal.getLogicClock())) {
								won = masterProposal.getNode();
								masterProposalQueue.clear();
								return won;
							}
						}
						outofelection.put(masterProposal.getProposer(), masterProposal.getNode());
						if (isWon(outofelection, masterProposal, winSize)
								&& checkMaster(outofelection, masterProposal, IGNOREVALUE)) {
							synchronized (this) {
								logicClock.set(masterProposal.getLogicClock());
							}
							won = masterProposal.getNode();
							masterProposalQueue.clear();
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
		}
		return won;
	}

	protected boolean isMember(NodeInfo node) {
		return needDiscoveryNodeInfos.containsKey(node.getName());
	}

	protected boolean isWon(Map<String, NodeInfo> masterProposalScoreMap, MasterProposal masterProposal, int wonVotes) {
		if (!isMember(masterProposal.getNode())) {
			return false;
		} else {
			int totalVotes = 0;
			for (Map.Entry<String, NodeInfo> entry : masterProposalScoreMap.entrySet()) {
				if (masterProposal.getNode().equals(entry.getValue())) {
					totalVotes++;
				}
			}
			return totalVotes >= wonVotes;
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

	private boolean allPingSuccessed() {
		SlaveNodeDiscoveryProtocol slaveNodeDiscoveryProtocol = null;
		final AtomicInteger successCount = new AtomicInteger(0);
		CountDownLatch cdl = new CountDownLatch(needDiscoveryNodeInfos.size());
		for (NodeInfo nodeInfo : needDiscoveryNodeInfos.values()) {
			try {
				slaveNodeDiscoveryProtocol = getNodeProtocolManager().lookupNodeRpcProtocol(nodeInfo,
						SlaveNodeDiscoveryProtocol.class, result -> {
							if (result.isSuccessed()) {
								successCount.incrementAndGet();
							}
							cdl.countDown();
						});
				slaveNodeDiscoveryProtocol.ping();
			} catch (Exception e) {
				log.error("rpc rpcNodeDiscoveryProtocol.sendMasterProposal exception", e);
			}
		}
		try {
			cdl.await();
		} catch (InterruptedException e) {
		}
		return successCount.get() == needDiscoveryNodeInfos.size();
	}

	private void sendMasterProposal() {
		RpcNodeDiscoveryProtocol rpcNodeDiscoveryProtocol = null;
		for (NodeInfo nodeInfo : needDiscoveryNodeInfos.values()) {
			try {
				rpcNodeDiscoveryProtocol = getNodeProtocolManager().lookupNodeRpcProtocol(nodeInfo,
						RpcNodeDiscoveryProtocol.class, result -> {
							if (result.isSuccessed()) {
								log.info("rpc rpcNodeDiscoveryProtocol.sendMasterProposal[" + nodeInfo + "]successed");
							} else {
								log.warn("rpc rpcNodeDiscoveryProtocol.sendMasterProposal[" + nodeInfo + "] failed");
							}
						});
				rpcNodeDiscoveryProtocol.sendMasterProposal(currentMasterProposal);
			} catch (Exception e) {
				log.error("rpc rpcNodeDiscoveryProtocol.sendMasterProposal exception", e);
			}
		}
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

	@Override
	public void broadcastLocalNodeInfo() {

	}

	@Override
	public void unbroadcastLocalNodeInfo() {

	}

	@Override
	protected void close() {

	}

	@Override
	protected void doHeartbeat(NodeInfo masterNodeInfo, NodeInfo localNodeInfo) {
		RpcNodeDiscoveryProtocol rpcNodeDiscoveryProtocol = getNodeProtocolManager()
				.lookupNodeRpcProtocol(masterNodeInfo, RpcNodeDiscoveryProtocol.class);
		rpcNodeDiscoveryProtocol.heartbeat(localNodeInfo);
	}

	public class RpcNodeDiscoveryProtocolImpl implements RpcNodeDiscoveryProtocol {

		@Override
		public String getName() {
			return getLocalNodeName();
		}

		@Override
		public void sendMasterProposal(MasterProposal masterProposal) {
			if (null != masterProposal) {
				masterProposalQueue.add(masterProposal);
			}
		}

		@Override
		public void heartbeat(NodeInfo nodeInfo) {
			refreshJoinNode(nodeInfo);
		}

	}
}
