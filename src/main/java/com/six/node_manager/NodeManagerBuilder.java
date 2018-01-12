package com.six.node_manager;

import com.six.node_manager.core.ClusterNodeManager;
import com.six.node_manager.core.StandAloneNodeManager;
import com.six.node_manager.discovery.impl.RpcNodeDiscovery;
import com.six.node_manager.election.impl.FastLeaderElection;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class NodeManagerBuilder {

	private boolean clusterEnable;
	private String clusterName;
	private String nodeName;
	private String host;
	private int port;

	public void setClusterEnable(boolean clusterEnable) {
		this.clusterEnable = clusterEnable;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public void setLocalHost(String host) {
		this.host = host;
	}

	public void setLocalPort(int port) {
		this.port = port;
	}

	public NodeInfo buildLocalNodeInfo() {
		NodeInfo localNodeInfo = new NodeInfo();
		localNodeInfo.setClusterName(clusterName);
		localNodeInfo.setName(nodeName);
		localNodeInfo.setHost(host);
		localNodeInfo.setPort(port);
		return localNodeInfo;
	}

	public NodeManager build() {
		if (clusterEnable) {
			return new ClusterNodeManager(buildLocalNodeInfo(), new FastLeaderElection(), new RpcNodeDiscovery());
		} else {
			return new StandAloneNodeManager();
		}
	}
}
