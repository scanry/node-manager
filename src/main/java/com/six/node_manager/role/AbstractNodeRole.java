package com.six.node_manager.role;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeResourceCollect;
import com.six.node_manager.RemoteAdapter;
import com.six.node_manager.core.ClusterNodes;

/**
 * @author sixliu
 * @date 2018年1月22日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractNodeRole implements NodeRole {

	static Logger log = LoggerFactory.getLogger(AbstractNodeRole.class);
	private NodeInfo master;
	private ClusterNodes clusterNodes;
	private RemoteAdapter remoteAdapter;
	private NodeResourceCollect nodeResourceCollect;
	// 从节点向主节点心跳间隔
	private long heartbeatInterval;
	// 允许从节点向主节点心跳异常次数
	private int allowHeartbeatErrCount;
	private long allowMaxHeartbeatInterval;

	public AbstractNodeRole(RemoteAdapter remoteAdapter, NodeResourceCollect nodeResourceCollect, NodeInfo master,
			ClusterNodes clusterNodes, long heartbeatInterval, int allowHeartbeatErrCount) {
		Objects.requireNonNull(remoteAdapter);
		Objects.requireNonNull(nodeResourceCollect);
		Objects.requireNonNull(master);
		Objects.requireNonNull(clusterNodes);
		this.remoteAdapter = remoteAdapter;
		this.nodeResourceCollect = nodeResourceCollect;
		this.master = master;
		this.clusterNodes = clusterNodes;
		this.heartbeatInterval = heartbeatInterval;
		this.allowHeartbeatErrCount = allowHeartbeatErrCount;
		this.allowMaxHeartbeatInterval = allowHeartbeatErrCount * heartbeatInterval;
	}

	@Override
	public final Node getNode() {
		return clusterNodes.getLocalNode();
	}

	@Override
	public final NodeInfo getMaster() {
		return master;
	}

	@Override
	public final ClusterNodes getClusterNodes() {
		return clusterNodes;
	}

	protected boolean isMember(NodeInfo node) {
		return getClusterNodes().isMember(node.getName());
	}

	protected RemoteAdapter getRemoteAdapter() {
		return remoteAdapter;
	}

	protected NodeResourceCollect getNodeResourceCollect() {
		return nodeResourceCollect;
	}

	protected long getHeartbeatInterval() {
		return heartbeatInterval;
	}

	protected int getAllowHeartbeatErrCount() {
		return allowHeartbeatErrCount;
	}

	protected long getAllowMaxHeartbeatInterval() {
		return allowMaxHeartbeatInterval;
	}
}
