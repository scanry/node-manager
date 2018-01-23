package com.six.node_manager;

import java.util.HashMap;
import java.util.Map;

import com.six.node_manager.core.ClusterNodeManager;
import com.six.node_manager.core.ClusterNodes;
import com.six.node_manager.core.Node;
import com.six.node_manager.core.NodeProtocolManagerImpl;
import com.six.node_manager.core.NodeResourceCollectFactory;
import com.six.node_manager.discovery.RpcNodeDiscovery;

import lombok.Data;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
@Data
public class NodeManagerBuilder {

	private static final long DEFAULT_HEARTBEAT_INTERVAL = 2000;
	private static final int DEFAULT_HEARTBEAT_ERR_COUNT = 2;
	private String clusterName;
	private String nodeName;
	private String host;
	private int port;
	private int version;
	/** name1@127.0.0.1:8881;name2@127.0.0.1:8882 **/
	private String discoveryNodes;
	private long heartbeatInterval;
	private int allowHeartbeatErrCount;
	private NodeResourceCollect nodeResourceCollect;
	private NodeProtocolManager nodeProtocolManager;
	private NodeDiscovery nodeDiscovery;

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
		Map<String, NodeInfo> needDiscoveryNodeInfos = paserNeedDiscoveryNodeInfos(discoveryNodes, localNodeInfo);
		if (0 >= heartbeatInterval) {
			heartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
		}
		if (0 >= allowHeartbeatErrCount) {
			allowHeartbeatErrCount = DEFAULT_HEARTBEAT_ERR_COUNT;
		}
		if (null == nodeResourceCollect) {
			nodeResourceCollect = NodeResourceCollectFactory.newNodeResourceCollect();
		}
		if (null == nodeProtocolManager) {
			nodeProtocolManager = new NodeProtocolManagerImpl(localNodeInfo.getHost(), localNodeInfo.getPort());
		}
		Node localNode = Node.getNode(localNodeInfo, needDiscoveryNodeInfos.size(), nodeResourceCollect);
		ClusterNodes clusterNodes = new ClusterNodes(localNode, needDiscoveryNodeInfos);
		if (null == nodeDiscovery) {
			nodeDiscovery = new RpcNodeDiscovery(clusterNodes, needDiscoveryNodeInfos, nodeProtocolManager,
					heartbeatInterval, allowHeartbeatErrCount);
		}
		return new ClusterNodeManager(clusterNodes, nodeProtocolManager, nodeDiscovery);
	}

	private Map<String, NodeInfo> paserNeedDiscoveryNodeInfos(String discoveryNodes, NodeInfo localNodeInfo) {
		Map<String, NodeInfo> needDiscoveryNodeInfos = null;
		if (null != discoveryNodes && discoveryNodes.trim().length() > 0) {
			String[] discoveryNodesArray = discoveryNodes.split(";");
			needDiscoveryNodeInfos = new HashMap<>(discoveryNodesArray.length);
			String[] discoveryNodeArray = null;
			NodeInfo nodeInfo = null;
			String nodeName = null;
			String nodeHostAndPort = null;
			for (String discoveryNodeStr : discoveryNodesArray) {
				nodeName = discoveryNodeStr.substring(0, discoveryNodeStr.indexOf("@"));
				nodeHostAndPort = discoveryNodeStr.substring(discoveryNodeStr.indexOf("@") + 1);
				discoveryNodeArray = nodeHostAndPort.split(":");
				if (!localNodeInfo.getName().equals(nodeName)) {
					nodeInfo = new NodeInfo();
					nodeInfo.setClusterName(clusterName);
					nodeInfo.setName(nodeName);
					nodeInfo.setHost(discoveryNodeArray[0]);
					nodeInfo.setPort(Integer.valueOf(discoveryNodeArray[1]));
					needDiscoveryNodeInfos.put(nodeInfo.getName(), nodeInfo);
				}
			}
		} else {
			needDiscoveryNodeInfos = new HashMap<>(1);
		}
		needDiscoveryNodeInfos.put(localNodeInfo.getName(), localNodeInfo);
		return needDiscoveryNodeInfos;
	}
}
