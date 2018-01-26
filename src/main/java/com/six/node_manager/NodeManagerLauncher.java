package com.six.node_manager;

import java.util.HashMap;
import java.util.Map;

import com.six.node_manager.core.ClusterNodeManager;
import com.six.node_manager.core.ClusterNodes;
import com.six.node_manager.core.NodeEventManagerImpl;
import com.six.node_manager.core.NodeProtocolManagerImpl;
import com.six.node_manager.core.NodeResourceCollectFactory;
import com.six.node_manager.core.SpiExtension;
import com.six.node_manager.discovery.RpcNodeDiscovery;
import com.six.node_manager.role.Node;


/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class NodeManagerLauncher{

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
	private NodeEventManager nodeEventManager;
	private NodeResourceCollect nodeResourceCollect;
	private NodeProtocolManager nodeProtocolManager;
	private NodeDiscovery nodeDiscovery;
	private NodeManager nodeManager;
	
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
		if(null==nodeManager) {
			synchronized (NodeManagerLauncher.class) {
				if(null==nodeManager) {
					if(null==nodeEventManager) {
						nodeEventManager=new NodeEventManagerImpl();
					}
					SpiExtension.getInstance().register(NodeEventManager.class,nodeEventManager);
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
					SpiExtension.getInstance().register(NodeResourceCollect.class,nodeResourceCollect);
					if (null == nodeProtocolManager) {
						nodeProtocolManager = new NodeProtocolManagerImpl(localNodeInfo.getHost(), localNodeInfo.getPort());
					}
					SpiExtension.getInstance().register(NodeProtocolManager.class,nodeProtocolManager);
					Node localNode = Node.getNode(localNodeInfo, needDiscoveryNodeInfos.size());
					ClusterNodes clusterNodes = new ClusterNodes(localNode, needDiscoveryNodeInfos);
					if (null == nodeDiscovery) {
						nodeDiscovery = new RpcNodeDiscovery(clusterNodes, needDiscoveryNodeInfos,
								heartbeatInterval, allowHeartbeatErrCount);
					}
					SpiExtension.getInstance().register(NodeDiscovery.class,nodeDiscovery);
					nodeManager=new ClusterNodeManager();
				}
			}
		}
		return nodeManager;
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
	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getDiscoveryNodes() {
		return discoveryNodes;
	}

	public void setDiscoveryNodes(String discoveryNodes) {
		this.discoveryNodes = discoveryNodes;
	}

	public long getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public void setHeartbeatInterval(long heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}

	public int getAllowHeartbeatErrCount() {
		return allowHeartbeatErrCount;
	}

	public void setAllowHeartbeatErrCount(int allowHeartbeatErrCount) {
		this.allowHeartbeatErrCount = allowHeartbeatErrCount;
	}

	public NodeEventManager getNodeEventManager() {
		return nodeEventManager;
	}

	public void setNodeEventManager(NodeEventManager nodeEventManager) {
		this.nodeEventManager = nodeEventManager;
	}

	public NodeResourceCollect getNodeResourceCollect() {
		return nodeResourceCollect;
	}

	public void setNodeResourceCollect(NodeResourceCollect nodeResourceCollect) {
		this.nodeResourceCollect = nodeResourceCollect;
	}

	public NodeProtocolManager getNodeProtocolManager() {
		return nodeProtocolManager;
	}

	public void setNodeProtocolManager(NodeProtocolManager nodeProtocolManager) {
		this.nodeProtocolManager = nodeProtocolManager;
	}

	public NodeDiscovery getNodeDiscovery() {
		return nodeDiscovery;
	}

	public void setNodeDiscovery(NodeDiscovery nodeDiscovery) {
		this.nodeDiscovery = nodeDiscovery;
	}

	public NodeManager getNodeManager() {
		return nodeManager;
	}

	public void setNodeManager(NodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}
}
