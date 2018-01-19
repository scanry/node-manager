package com.six.node_manager;

import java.util.Set;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description 节点注册中心
 */
public interface NodeDiscovery extends Service {

	String getClusterName();

	String getLocalNodeName();
	
	NodeState getNodeState();
	
	NodeInfo getLocalNodeInfo();

	NodeInfo getMasterNodeInfo();

	Set<NodeInfo> getSlaveNodInfos();
	
	boolean isHealthy();

	void join(NodeInfo slaveNodeInfo);

	void leave(NodeInfo slaveNodeInfo);

	void broadcastLocalNodeInfo();

	void unbroadcastLocalNodeInfo();
}
