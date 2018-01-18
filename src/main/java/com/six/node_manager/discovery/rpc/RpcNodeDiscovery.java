package com.six.node_manager.discovery.rpc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeEventManager;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
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
	private MasterProposal currentMasterProposal;
	private AtomicInteger logicClock = new AtomicInteger(0);
	private final static int maxNotificationInterval = 60000;

	public RpcNodeDiscovery(NodeInfo localNodeInfo, Set<NodeInfo> needDiscoveryNodeInfos,
			NodeProtocolManager nodeProtocolManager, NodeEventManager nodeEventManager, long heartbeatInterval,
			int allowHeartbeatErrCount, long electionInterval) {
		super(localNodeInfo, needDiscoveryNodeInfos, nodeProtocolManager, nodeEventManager, heartbeatInterval,
				allowHeartbeatErrCount, electionInterval);
		nodeProtocolManager.registerNodeRpcProtocol(RpcNodeDiscoveryProtocol.class, new RpcNodeDiscoveryProtocolImpl());
	}

	@Override
	protected NodeInfo doElection() {
		NodeInfo won = null;
		NodeInfo proposer = getLocalNodeInfo();
		NodeInfo defaultMasterNode = proposer;
		List<NodeInfo> needDiscoveryNodeInfos = getNeedDiscoveryNodeInfos();
		if (null == needDiscoveryNodeInfos || needDiscoveryNodeInfos.isEmpty()) {
			won = getLocalNodeInfo();
		} else {
			sendMasterProposal(needDiscoveryNodeInfos, proposer, defaultMasterNode);
			Map<NodeInfo, Set<String>> masterProposalScoreMap = new HashMap<>();
			int winSize = needDiscoveryNodeInfos.size() / 2 + 1;
			MasterProposal masterProposal = null;
			long notTimeout = 3000;
			while (null == won) {
				try {
					masterProposal = masterProposalQueue.poll(notTimeout, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
				}
				if (null == masterProposal) {
					if (allPingSuccessed(needDiscoveryNodeInfos)) {
						sendMasterProposal(needDiscoveryNodeInfos, proposer, defaultMasterNode);
					}
					long tmpTimeOut = notTimeout * 2;
					notTimeout = (tmpTimeOut < maxNotificationInterval ? tmpTimeOut : maxNotificationInterval);
					log.info("Notification time out: " + notTimeout);
				} else {
					if (masterProposal.getLogicClock() < currentMasterProposal.getLogicClock()) {
						log.warn("the MasterProposal{" + masterProposal.toString() + "}'s logicClock["
								+ masterProposal.getLogicClock() + "]<currentLogicClock["
								+ currentMasterProposal.getLogicClock() + "]");
					} else {
						if (masterProposal.getMaster().getVersion() > currentMasterProposal.getMaster().getVersion()) {
							this.currentMasterProposal = masterProposal;
						} else if (masterProposal.getMaster().getName()
								.compareTo(currentMasterProposal.getMaster().getName()) > 0) {
							this.currentMasterProposal = masterProposal;
						}
						Set<String> masterProposalSet = masterProposalScoreMap
								.computeIfAbsent(masterProposal.getMaster(), newKey -> new HashSet<>());
						masterProposalSet.add(masterProposal.getProposer());
						if (masterProposalSet.size() > winSize) {
							won = masterProposal.getMaster();
							masterProposalQueue.clear();
						}
					}
				}
			}
		}
		return won;
	}

	private boolean allPingSuccessed(List<NodeInfo> needDiscoveryNodeInfos) {
		SlaveNodeDiscoveryProtocol slaveNodeDiscoveryProtocol = null;
		final AtomicInteger successCount = new AtomicInteger(0);
		CountDownLatch cdl = new CountDownLatch(needDiscoveryNodeInfos.size());
		for (NodeInfo nodeInfo : needDiscoveryNodeInfos) {
			try {
				slaveNodeDiscoveryProtocol = getNodeProtocolManager().lookupNodeRpcProtocol(nodeInfo,
						SlaveNodeDiscoveryProtocol.class, result -> {
							if (result.isSuccessed()) {
								successCount.incrementAndGet();
							}
							cdl.countDown();
						});
				slaveNodeDiscoveryProtocol.ping();
			}catch (Exception e) {
				log.error("rpc rpcNodeDiscoveryProtocol.sendMasterProposal exception", e);
			}
		}
		try {
			cdl.await();
		} catch (InterruptedException e) {
		}
		return successCount.get() == needDiscoveryNodeInfos.size();
	}

	private void sendMasterProposal(List<NodeInfo> needDiscoveryNodeInfos, NodeInfo proposer, NodeInfo master) {
		RpcNodeDiscoveryProtocol rpcNodeDiscoveryProtocol = null;
		MasterProposal masterProposal = new MasterProposal();
		masterProposal.setProposer(proposer.getName());
		masterProposal.setMaster(master);
		masterProposal.setLogicClock(logicClock.getAndIncrement());
		this.currentMasterProposal = masterProposal;
		for (NodeInfo nodeInfo : needDiscoveryNodeInfos) {
			try {
				rpcNodeDiscoveryProtocol = getNodeProtocolManager().lookupNodeRpcProtocol(nodeInfo,
						RpcNodeDiscoveryProtocol.class, result -> {
							if(result.isSuccessed()) {
								log.info("rpc rpcNodeDiscoveryProtocol.sendMasterProposal successed");
							}else {
								log.warn("rpc rpcNodeDiscoveryProtocol.sendMasterProposal["+nodeInfo+"] failed");
							}
						});
				rpcNodeDiscoveryProtocol.sendMasterProposal(masterProposal);
			}catch (Exception e) {
				log.error("rpc rpcNodeDiscoveryProtocol.sendMasterProposal exception", e);
			}
		}
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
				.lookupNodeRpcProtocol(masterNodeInfo, RpcNodeDiscoveryProtocol.class, null);
		rpcNodeDiscoveryProtocol.heartbeat(localNodeInfo);
	}

	class RpcNodeDiscoveryProtocolImpl implements RpcNodeDiscoveryProtocol {

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
