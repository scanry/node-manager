package com.six.node_manager.discovery.impl;

import java.util.List;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.discovery.NodeDiscovery;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class RpcNodeDiscovery implements NodeDiscovery {

	public RpcNodeDiscovery() {

	}

	@Override
	public String getClusterName() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void registerNodeInfo(NodeInfo local) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterNodeInfo(NodeInfo local) {
		// TODO Auto-generated method stub

	}

	@Override
	public NodeInfo getLocalNodeInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeInfo getMasterNodeInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NodeInfo> getSlaveNodInfos() {
		// TODO Auto-generated method stub
		return null;
	}

}
