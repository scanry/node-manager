package com.six.node_manager;

import java.util.List;
import java.util.Set;

import com.six.node_manager.role.NodeRole;
import com.six.node_manager.service.Service;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description 节点发现
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

	boolean isHealthy();
}
