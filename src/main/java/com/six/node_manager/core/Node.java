package com.six.node_manager.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeState;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class Node {

	private final String clusterName;
	private final String name;
	private final String host;
	private final int port;
	private AtomicLong version;
	private AtomicReferenceFieldUpdater<Node, NodeState> ATOMIC_STATE = AtomicReferenceFieldUpdater
			.newUpdater(Node.class, NodeState.class, "state");
	private volatile NodeState state = NodeState.LOOKING;

	public Node(String clusterName, String name, String host, int port, long version) {
		this.clusterName = clusterName;
		this.name = name;
		this.host = host;
		this.port = port;
		this.version = new AtomicLong(version);
	}

	public boolean isLooking() {
		return NodeState.LOOKING == ATOMIC_STATE.get(this);
	}

	public void looking() {
		ATOMIC_STATE.set(this, NodeState.LOOKING);
	}

	public boolean isMaster() {
		return NodeState.MASTER == ATOMIC_STATE.get(this);
	}

	public void master() {
		ATOMIC_STATE.set(this, NodeState.MASTER);
	}

	public boolean isSlave() {
		return NodeState.SLAVE == ATOMIC_STATE.get(this);
	}

	public void slave() {
		ATOMIC_STATE.set(this, NodeState.SLAVE);
	}

	public long getVersion() {
		return version.get();
	}

	public void incVersion() {
		version.incrementAndGet();
	}

	public NodeInfo NodeInfo() {
		NodeInfo nodeInfo = new NodeInfo();
		nodeInfo.setClusterName(clusterName);
		nodeInfo.setName(name);
		nodeInfo.setHost(host);
		nodeInfo.setPort(port);
		nodeInfo.setState(state);
		nodeInfo.setVersion(getVersion());
		return nodeInfo;
	}
}
