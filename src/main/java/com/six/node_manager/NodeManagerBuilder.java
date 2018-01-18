package com.six.node_manager;

import java.util.HashSet;
import java.util.Set;

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
			Set<NodeInfo> needDiscoveryNodeInfos = paserNeedDiscoveryNodeInfos(discoveryNodes, localNodeInfo);
			return new ClusterNodeManager(localNodeInfo, needDiscoveryNodeInfos);
		} else {
			return new StandAloneNodeManager();
		}
	}

	private static Set<NodeInfo> paserNeedDiscoveryNodeInfos(String discoveryNodes, NodeInfo localNodeInfo) {
		Set<NodeInfo> needDiscoveryNodeInfos = null;
		if (null != discoveryNodes && discoveryNodes.trim().length() > 0) {
			String[] discoveryNodesArray = discoveryNodes.split(";");
			needDiscoveryNodeInfos = new HashSet<>(discoveryNodesArray.length);
			String[] discoveryNodeArray = null;
			NodeInfo nodeInfo = null;
			for (String discoveryNodeStr : discoveryNodesArray) {
				discoveryNodeArray = discoveryNodeStr.split(":");
				if (!(localNodeInfo.getHost().equals(discoveryNodeArray[0])
						&& localNodeInfo.getPort() == Integer.valueOf(discoveryNodeArray[1]))) {
					nodeInfo = new NodeInfo();
					nodeInfo.setHost(discoveryNodeArray[0]);
					nodeInfo.setPort(Integer.valueOf(discoveryNodeArray[1]));
					needDiscoveryNodeInfos.add(nodeInfo);
				}
			}
		} else {
			needDiscoveryNodeInfos = new HashSet<>(1);
		}
		needDiscoveryNodeInfos.add(localNodeInfo);
		return needDiscoveryNodeInfos;
	}
}
