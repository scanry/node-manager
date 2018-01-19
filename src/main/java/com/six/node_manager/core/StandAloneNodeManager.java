package com.six.node_manager.core;

import java.util.Set;

import com.six.node_manager.Cache;
import com.six.node_manager.FileSystem;
import com.six.node_manager.Lock;
import com.six.node_manager.NodeEventManager;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeManager;
import com.six.node_manager.NodeProtocolManager;


/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class StandAloneNodeManager extends AbstractNodeManager implements NodeManager {


	@Override
	public String getClusterName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeInfo getLocalNodeInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeInfo getMasterNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<NodeInfo> getSlaveNods() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeEventManager getNodeEventManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doStart() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doStop() {
		// TODO Auto-generated method stub

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

}
