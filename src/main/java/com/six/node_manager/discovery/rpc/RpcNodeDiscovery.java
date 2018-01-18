package com.six.node_manager.discovery.rpc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeEventManager;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.discovery.AbstractNodeDiscovery;

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
	private AtomicInteger logicClock;

	public RpcNodeDiscovery(NodeInfo localNodeInfo, List<NodeInfo> needDiscoveryNodeInfos,
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
			while (null == won) {
				try {
					masterProposal = masterProposalQueue.poll(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
				}
				if (null == masterProposal) {
					sendMasterProposal(needDiscoveryNodeInfos, proposer, defaultMasterNode);
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

	private void sendMasterProposal(List<NodeInfo> needDiscoveryNodeInfos, NodeInfo proposer, NodeInfo master) {
		RpcNodeDiscoveryProtocol rpcNodeDiscoveryProtocol = null;
		MasterProposal masterProposal = new MasterProposal();
		masterProposal.setProposer(proposer.getName());
		masterProposal.setMaster(master);
		masterProposal.setLogicClock(logicClock.getAndIncrement());
		this.currentMasterProposal = masterProposal;
		for (NodeInfo nodeInfo : needDiscoveryNodeInfos) {
			rpcNodeDiscoveryProtocol = getNodeProtocolManager().lookupNodeRpcProtocol(nodeInfo,
					RpcNodeDiscoveryProtocol.class, null);
			rpcNodeDiscoveryProtocol.sendMasterProposal(masterProposal);
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
