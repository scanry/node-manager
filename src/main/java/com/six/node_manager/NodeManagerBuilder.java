package com.six.node_manager;

import java.util.HashMap;
import java.util.Map;

import com.six.node_manager.core.ClusterNodeManager;

import lombok.Data;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
@Data
public class NodeManagerBuilder {

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
		Map<String,NodeInfo> needDiscoveryNodeInfos = paserNeedDiscoveryNodeInfos(discoveryNodes, localNodeInfo);
		return new ClusterNodeManager(localNodeInfo, needDiscoveryNodeInfos);
	}

	// name@127.0.0.1:8881
	private static Map<String,NodeInfo> paserNeedDiscoveryNodeInfos(String discoveryNodes, NodeInfo localNodeInfo) {
		Map<String,NodeInfo> needDiscoveryNodeInfos = null;
		if (null != discoveryNodes && discoveryNodes.trim().length() > 0) {
			String[] discoveryNodesArray = discoveryNodes.split(";");
			needDiscoveryNodeInfos = new HashMap<>(discoveryNodesArray.length);
			String[] discoveryNodeArray = null;
			NodeInfo nodeInfo = null;
			String nodeName=null;
			String nodeHostAndPort=null;
			for (String discoveryNodeStr : discoveryNodesArray) {
				nodeName=discoveryNodeStr.substring(0, discoveryNodeStr.indexOf("@"));
				nodeHostAndPort=discoveryNodeStr.substring(discoveryNodeStr.indexOf("@")+1);
				discoveryNodeArray = nodeHostAndPort.split(":");
				if (!localNodeInfo.getName().equals(nodeName)) {
					nodeInfo = new NodeInfo();
					nodeInfo.setName(nodeName);
					nodeInfo.setHost(discoveryNodeArray[0]);
					nodeInfo.setPort(Integer.valueOf(discoveryNodeArray[1]));
					needDiscoveryNodeInfos.put(nodeInfo.getName(),nodeInfo);
				}
			}
		} else {
			needDiscoveryNodeInfos = new HashMap<>(1);
		}
		needDiscoveryNodeInfos.put(localNodeInfo.getName(),localNodeInfo);
		return needDiscoveryNodeInfos;
	}
}
