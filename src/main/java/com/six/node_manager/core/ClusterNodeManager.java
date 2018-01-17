package com.six.node_manager.core;

import java.util.List;

import com.six.node_manager.Cache;
import com.six.node_manager.FileSystem;
import com.six.node_manager.Lock;
import com.six.node_manager.NodeDiscovery;
import com.six.node_manager.NodeEventManager;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.discovery.RpcNodeDiscovery;
import com.six.node_manager.protocol.MasterNodeProtocol;
import com.six.node_manager.protocol.impl.MasterNodeProtocolImpl;


/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class ClusterNodeManager extends AbstractNodeManager {

	private NodeDiscovery nodeDiscovery;
	private NodeEventManager nodeEventManager;
	private NodeProtocolManager nodeProtocolManager;

	public ClusterNodeManager(NodeInfo localNodeInfo, List<NodeInfo> needDiscoveryNodeInfos) {
		this.nodeEventManager = new NodeEventManagerImpl();
		this.nodeDiscovery = new RpcNodeDiscovery(localNodeInfo, null, this);
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
	public NodeEventManager getNodeEventManager() {
		return nodeEventManager;
	}

	@Override
	public NodeProtocolManager getNodeProtocolManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cache newCache(String cacheName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Lock newLock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileSystem getFileSystem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doStart() {
		nodeProtocolManager.start();
		nodeProtocolManager.registerNodeRpcProtocol(MasterNodeProtocol.class, new MasterNodeProtocolImpl());
		nodeDiscovery.start();
	}

	@Override
	protected void doStop() {
		MasterNodeProtocol masterNodeProtocol = nodeProtocolManager
				.lookupNodeRpcProtocol(nodeDiscovery.getMasterNodeInfo(), MasterNodeProtocol.class);
		masterNodeProtocol.leave(nodeDiscovery.getLocalNodeInfo());
		if (null != nodeProtocolManager) {
			nodeProtocolManager.stop();
		}
	}
}
