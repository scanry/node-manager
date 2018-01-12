package com.six.node_manager.core;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import com.six.node_manager.NodeEvent;
import com.six.node_manager.NodeEventListen;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.discovery.NodeDiscovery;
import com.six.node_manager.election.LeaderElection;
import com.six.node_manager.protocol.MasterNodeProtocol;
import com.six.node_manager.protocol.SlaveNodeProtocol;
import com.six.node_manager.protocol.impl.MasterNodeProtocolImpl;
import com.six.node_manager.protocol.impl.SlaveNodeProtocolImpl;

import six.com.rpc.AsyCallback;
import six.com.rpc.RpcClient;
import six.com.rpc.client.NettyRpcCilent;
import six.com.rpc.server.NettyRpcServer;
import six.com.rpc.server.RpcServer;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class ClusterNodeManager extends AbstractNodeManager {

	private LeaderElection leaderElection;
	private NodeDiscovery nodeDiscovery;
	private RpcServer server;
	private RpcClient client;
	private Map<NodeEvent, Set<NodeEventListen>> nodeEventListens = new ConcurrentHashMap<>();

	public ClusterNodeManager(NodeInfo localNodeInfo, LeaderElection leaderElection, NodeDiscovery nodeDiscovery) {
		super(localNodeInfo);
		Objects.requireNonNull(leaderElection);
		Objects.requireNonNull(nodeDiscovery);
		this.leaderElection = leaderElection;
		this.nodeDiscovery = nodeDiscovery;
		server = new NettyRpcServer(localNodeInfo.getHost(), localNodeInfo.getPort());
		server.register(MasterNodeProtocol.class, new MasterNodeProtocolImpl());
		server.register(SlaveNodeProtocol.class, new SlaveNodeProtocolImpl());
		client = new NettyRpcCilent();
	}

	@Override
	public String getClusterName() {
		return nodeDiscovery.getClusterName();
	}

	@Override
	public final NodeInfo getLocalNodeInfo() {
		return nodeDiscovery.getLocalNodeInfo();
	}

	@Override
	public NodeInfo getMasterNode() {
		return nodeDiscovery.getMasterNodeInfo();
	}

	@Override
	public List<NodeInfo> getSlaveNods() {
		return nodeDiscovery.getSlaveNodInfos();
	}

	@Override
	public void registerNodeEventListen(NodeEvent event, NodeEventListen nodeListen) {
		Objects.requireNonNull(event);
		Objects.requireNonNull(nodeListen);
		nodeEventListens.computeIfAbsent(event, newKey -> new HashSet<>()).add(nodeListen);
	}

	@Override
	public void unregisterNodeEventListen(NodeEvent event, NodeEventListen nodeListen) {
		Set<NodeEventListen> set = nodeEventListens.get(event);
		if (null != set) {
			set.remove(nodeListen);
		}
	}

	@Override
	public void registerNodeRpcProtocol(ExecutorService executorService, Class<?> protocol) {
		server.register(executorService, MasterNodeProtocol.class, new MasterNodeProtocolImpl());
	}

	@Override
	public void unregisterNodeRpcProtocol(Class<?> protocol) {
		server.unregister(protocol);
	}

	@Override
	public <T> T lookupNodeRpcProtocol(NodeInfo node, Class<?> protocol) {
		Objects.requireNonNull(node);
		return client.lookupService(node.getHost(), node.getPort(), protocol);
	}

	@Override
	public <T> T lookupNodeRpcProtocol(NodeInfo node, Class<?> protocol, AsyCallback callback) {
		Objects.requireNonNull(node);
		return client.lookupService(node.getHost(), node.getPort(), protocol, callback);
	}

	@Override
	public void start() {
		server.start();
		NodeInfo localNodeInfo = getLocalNodeInfo();
		nodeDiscovery.registerNodeInfo(localNodeInfo);
		NodeInfo masterNodeInfo = nodeDiscovery.getMasterNodeInfo();
		if (null == masterNodeInfo) {
			masterNodeInfo = leaderElection.election(localNodeInfo);
			afterElection(masterNodeInfo);
		}
	}

	private void afterElection(NodeInfo masterNodeInfo) {
		NodeInfo localNodeInfo = getLocalNodeInfo();
		if (!localNodeInfo.equals(masterNodeInfo)) {
			getLocalNode().slave();
			MasterNodeProtocol masterNodeProtocol = lookupNodeRpcProtocol(masterNodeInfo, MasterNodeProtocol.class);
			masterNodeProtocol.join(localNodeInfo);
		} else {
			getLocalNode().master();
		}
	}

	@Override
	public void shutdown() {
		MasterNodeProtocol masterNodeProtocol = lookupNodeRpcProtocol(nodeDiscovery.getMasterNodeInfo(),
				MasterNodeProtocol.class);
		masterNodeProtocol.leave(nodeDiscovery.getLocalNodeInfo());
		if (null != leaderElection) {
			leaderElection.close();
		}
		if (null != server) {
			server.shutdown();
		}
		if (null != client) {
			client.shutdown();
		}
	}

}
