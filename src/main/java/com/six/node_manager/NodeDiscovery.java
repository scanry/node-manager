package com.six.node_manager;

import java.util.List;

import com.six.node_manager.core.Node;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description 节点注册中心
 */
public interface NodeDiscovery extends Service{

	String getClusterName();

	Node getLocalNode();
	
	NodeInfo getLocalNodeInfo();
	
	List<NodeInfo> getNeedDiscoveryNodeInfos();

	NodeInfo getMasterNodeInfo();

	List<NodeInfo> getSlaveNodInfos();

	void broadcastLocalNodeInfo();

	void unbroadcastLocalNodeInfo();
}
