package com.six.node_manager.core;

import java.util.Map;
import java.util.Set;

import com.six.node_manager.Cache;
import com.six.node_manager.FileSystem;
import com.six.node_manager.Lock;
import com.six.node_manager.NodeDiscovery;
import com.six.node_manager.NodeEventManager;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.NodeResourceCollect;
import com.six.node_manager.discovery.RpcNodeDiscovery;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class ClusterNodeManager extends AbstractNodeManager {

	protected Node localNode;
	private NodeDiscovery nodeDiscovery;
	private NodeProtocolManager nodeProtocolManager;
	private NodeResourceCollect nodeResourceCollect;

	public ClusterNodeManager(NodeInfo localNodeInfo, Map<String, NodeInfo> needDiscoveryNodeInfos,
			long heartbeatInterval, int allowHeartbeatErrCount) {
		this.localNode = Node.getNode(localNodeInfo);
		this.nodeProtocolManager = new NodeProtocolManagerImpl(localNodeInfo.getHost(), localNodeInfo.getPort());
		this.nodeResourceCollect = new NodeResourceCollectImpl();
		this.nodeDiscovery = new RpcNodeDiscovery(localNode, needDiscoveryNodeInfos, nodeProtocolManager,
				nodeResourceCollect, heartbeatInterval, allowHeartbeatErrCount);
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
	public Set<NodeInfo> getSlaveNods() {
		return nodeDiscovery.getSlaveNodInfos();
	}

	@Override
	public NodeProtocolManager getNodeProtocolManager() {
		return nodeProtocolManager;
	}
	
	@Override
	public NodeEventManager getNodeEventManager() {
		return localNode.getNodeEventManager();
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
		localNode.start();
		nodeProtocolManager.start();
		nodeDiscovery.start();
	}

	@Override
	protected void doStop() {
		if (null != nodeDiscovery) {
			nodeDiscovery.stop();
		}
		if (null != nodeProtocolManager) {
			nodeProtocolManager.stop();
		}
		if (null != localNode) {
			localNode.stop();
		}
	}
}
