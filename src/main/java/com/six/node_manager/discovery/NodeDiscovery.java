package com.six.node_manager.discovery;

import java.util.List;

import com.six.node_manager.NodeInfo;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description 节点注册中心
 */
public interface NodeDiscovery {

	String getClusterName();

	NodeInfo getLocalNodeInfo();

	NodeInfo getMasterNodeInfo();

	List<NodeInfo> getSlaveNodInfos();

	void registerNodeInfo(NodeInfo local);

	void unregisterNodeInfo(NodeInfo local);

}
