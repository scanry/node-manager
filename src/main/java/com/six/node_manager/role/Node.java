package com.six.node_manager.role;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeStatus;
import com.six.node_manager.core.NodeEventManager;
import com.six.node_manager.NodeEvent;
import com.six.node_manager.NodeEventType;

/**
 * @author sixliu
 * @date 2018年1月22日
 * @email 359852326@qq.com
 * @Description
 */
public class Node{

	private static volatile Node single;
	private final String clusterName;
	private final String name;
	private final String host;
	private final int port;
	private AtomicLong version;
	private int nodeCount;
	private AtomicReferenceFieldUpdater<Node, NodeStatus> ATOMIC_STATE = AtomicReferenceFieldUpdater
			.newUpdater(Node.class, NodeStatus.class, "state");
	private volatile NodeStatus state = NodeStatus.LOOKING;
	private NodeEventManager nodeEventManager;

	public static Node getNode(NodeInfo nodeInfo, int nodeCount,NodeEventManager nodeEventManager) {
		if (null == single) {
			synchronized (Node.class) {
				if (null == single) {
					single = new Node(nodeInfo.getClusterName(), nodeInfo.getName(), nodeInfo.getHost(),
							nodeInfo.getPort(), nodeInfo.getVersion(), nodeCount,nodeEventManager);
				}
			}

		}
		return single;
	}

	private Node(String clusterName, String name, String host, int port, long version, int nodeCount,NodeEventManager nodeEventManager) {
		this.clusterName = clusterName;
		this.name = name;
		this.host = host;
		this.port = port;
		this.version = new AtomicLong(version);
		this.nodeCount = nodeCount;
		this.nodeEventManager=nodeEventManager;
	}

	public String getClusterName() {
		return clusterName;
	}

	public String getName() {
		return name;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public long getVersion() {
		return version.get();
	}

	public void incVersion() {
		version.incrementAndGet();
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public NodeStatus getNodeState() {
		return state;
	}

	public boolean isLooking() {
		return NodeStatus.LOOKING == ATOMIC_STATE.get(this);
	}

	public void looking() {
		ATOMIC_STATE.set(this, NodeStatus.LOOKING);
		nodeEventManager.addNodeEvent(new NodeEvent(NodeEventType.BECOME_LOOKING, nodeInfo()));
	}

	public boolean isMaster() {
		return NodeStatus.MASTER == ATOMIC_STATE.get(this);
	}

	public void master() {
		ATOMIC_STATE.set(this, NodeStatus.MASTER);
		if (isSlave()) {
			nodeEventManager.addNodeEvent(new NodeEvent(NodeEventType.RUNTIME_BECAOME_MASTER, nodeInfo()));
		} else {
			nodeEventManager.addNodeEvent(new NodeEvent(NodeEventType.INIT_BECOME_MASTER, nodeInfo()));
		}

	}

	public boolean isSlave() {
		return NodeStatus.SLAVE == ATOMIC_STATE.get(this);
	}

	public void slave() {
		ATOMIC_STATE.set(this, NodeStatus.SLAVE);
		nodeEventManager.addNodeEvent(new NodeEvent(NodeEventType.BECOME_SLAVE, nodeInfo()));
	}

	public NodeInfo nodeInfo() {
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
