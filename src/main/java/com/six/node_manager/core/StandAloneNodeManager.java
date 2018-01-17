package com.six.node_manager.core;

import java.util.List;
import java.util.concurrent.ExecutorService;

import com.six.node_manager.NodeEventManager;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeManager;

import six.com.rpc.AsyCallback;

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
	public List<NodeInfo> getSlaveNods() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerNodeRpcProtocol(ExecutorService executorService, Class<?> protocol) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterNodeRpcProtocol(Class<?> protocol) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T lookupNodeRpcProtocol(NodeInfo node, Class<?> protocol) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T lookupNodeRpcProtocol(NodeInfo node, Class<?> protocol, AsyCallback callback) {
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

}
