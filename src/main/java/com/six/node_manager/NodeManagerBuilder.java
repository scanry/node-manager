package com.six.node_manager;

import com.six.node_manager.core.ClusterNodeManager;
import com.six.node_manager.core.StandAloneNodeManager;

import lombok.Data;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
@Data
public class NodeManagerBuilder {

	private boolean clusterEnable;
	private String clusterName;
	private String nodeName;
	private String host;
	private int port;
	private int version;
	private String discoveryNodes;

	public NodeInfo buildLocalNodeInfo() {
		NodeInfo localNodeInfo = new NodeInfo();
		localNodeInfo.setClusterName(clusterName);
		localNodeInfo.setName(nodeName);
		localNodeInfo.setHost(host);
		localNodeInfo.setPort(port);
		localNodeInfo.setVersion(version);
		return localNodeInfo;
	}

	public NodeManager build() {
		NodeInfo localNodeInfo = buildLocalNodeInfo();
		if (clusterEnable) {
			return new ClusterNodeManager(localNodeInfo, null);
		} else {
			return new StandAloneNodeManager();
		}
	}
}
