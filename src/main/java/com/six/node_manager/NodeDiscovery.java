package com.six.node_manager;

import java.util.List;
import java.util.Set;

import com.six.node_manager.core.Service;
import com.six.node_manager.role.NodeRole;

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

	NodeRole getNodeRole();

	NodeInfo getMasterNodeInfo();

	Set<NodeInfo> getSlaveNodInfos();
	
	List<NodeInfo> listJoin();
	
	void join(NodeInfo nodeInfo);

	void refreshJoin(NodeResource nodeResource);

	void leave(NodeInfo Info);

	boolean isHealthy();
	
	void broadcastLocalNodeInfo();

	void unbroadcastLocalNodeInfo();
}
