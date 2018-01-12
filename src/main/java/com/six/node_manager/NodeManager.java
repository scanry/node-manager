package com.six.node_manager;

import java.util.List;
import java.util.concurrent.ExecutorService;

import six.com.rpc.AsyCallback;

/**
 * @author sixliu
 * @date 2017年12月21日
 * @email 359852326@qq.com
 * @Description
 */
public interface NodeManager{

	String getClusterName();

	NodeInfo getLocalNodeInfo();

	NodeInfo getMasterNode();

	List<NodeInfo> getSlaveNods();

	void registerNodeEventListen(NodeEvent event, NodeEventListen nodeListen);

	void unregisterNodeEventListen(NodeEvent event, NodeEventListen nodeListen);

	void registerNodeRpcProtocol(ExecutorService executorService, Class<?> protocol);

	void unregisterNodeRpcProtocol(Class<?> protocol);
	
	<T>T lookupNodeRpcProtocol(NodeInfo node, Class<?> protocol);
	
	<T>T lookupNodeRpcProtocol(NodeInfo node, Class<?> protocol,AsyCallback callback);

	void start();

	void shutdown();
}
