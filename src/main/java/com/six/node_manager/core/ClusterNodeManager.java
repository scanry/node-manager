package com.six.node_manager.core;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import com.six.node_manager.NodeDiscovery;
import com.six.node_manager.NodeEventManager;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.discovery.RpcNodeDiscovery;
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

	private NodeDiscovery nodeDiscovery;
	private RpcServer server;
	private RpcClient client;
	private NodeEventManager nodeEventManager;
	

	public ClusterNodeManager(NodeInfo localNodeInfo,List<NodeInfo> needDiscoveryNodeInfos) {
		server = new NettyRpcServer(localNodeInfo.getHost(), localNodeInfo.getPort());
		server.register(MasterNodeProtocol.class, new MasterNodeProtocolImpl());
		server.register(SlaveNodeProtocol.class, new SlaveNodeProtocolImpl());
		client = new NettyRpcCilent();
		nodeEventManager = new NodeEventManagerImpl();
		this.nodeDiscovery=new RpcNodeDiscovery(localNodeInfo,null,this);
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
	protected void doStart() {
		server.start();
		nodeDiscovery.start();
	}



	@Override
	public NodeEventManager getNodeEventManager() {
		return nodeEventManager;
	}

	@Override
	protected void doStop() {
		MasterNodeProtocol masterNodeProtocol = lookupNodeRpcProtocol(nodeDiscovery.getMasterNodeInfo(),
				MasterNodeProtocol.class);
		masterNodeProtocol.leave(nodeDiscovery.getLocalNodeInfo());
		if (null != server) {
			server.shutdown();
		}
		if (null != client) {
			client.shutdown();
		}
	}
}
