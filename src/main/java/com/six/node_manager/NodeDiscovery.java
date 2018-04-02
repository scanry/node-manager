package com.six.node_manager;

import java.util.List;

import com.six.node_manager.service.Service;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description 节点发现
 */
public interface NodeDiscovery extends Service {

	NodeInfo getLocalNodeInfo();

	NodeInfo getMasterNodeInfo();

	List<NodeInfo> getAllSlaveNodeInfos();
	
	List<NodeInfo> getOnineSlaveNodeInfos();
	
	List<NodeInfo> getOffineSlaveNodeInfos();

	void registerNodeEventListener(NodeEventType event, NodeEventListen nodeListen);
}
