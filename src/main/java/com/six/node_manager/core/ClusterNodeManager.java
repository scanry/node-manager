package com.six.node_manager.core;

import java.util.Set;

import com.six.node_manager.Cache;
import com.six.node_manager.ClusterInfo;
import com.six.node_manager.FileSystem;
import com.six.node_manager.Lock;
import com.six.node_manager.NodeDiscovery;
import com.six.node_manager.NodeEventManager;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeManager;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.service.AbstractService;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class ClusterNodeManager extends AbstractService implements NodeManager {

	private NodeEventManager nodeEventManager=SpiExtension.getInstance().find(NodeEventManager.class);
	private NodeProtocolManager nodeProtocolManager=SpiExtension.getInstance().find(NodeProtocolManager.class);
	private NodeDiscovery nodeDiscovery=SpiExtension.getInstance().find(NodeDiscovery.class);

	public ClusterNodeManager() {
		super("nodeManager");
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
	public ClusterInfo getClusterInfo() {
		return new ClusterInfo();
	}

	@Override
	public NodeProtocolManager getNodeProtocolManager() {
		return nodeProtocolManager;
	}

	@Override
	public NodeEventManager getNodeEventManager() {
		return nodeEventManager;
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
		SpiExtension.getInstance().startAll();
	}

	@Override
	protected void doStop() {
		SpiExtension.getInstance().stopAll();
	}
}
