package com.six.node_manager.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeState;
import com.six.node_manager.NodeEvent;
import com.six.node_manager.NodeEventListen;
import com.six.node_manager.NodeEventManager;
import com.six.node_manager.NodeEventType;

/**
 * @author sixliu
 * @date 2018年1月22日
 * @email 359852326@qq.com
 * @Description
 */
public class Node extends AbstractService implements NodeEventManager, Service {

	private static volatile Node single;
	private final String clusterName;
	private final String name;
	private final String host;
	private final int port;
	private AtomicLong version;
	private AtomicReferenceFieldUpdater<Node, NodeState> ATOMIC_STATE = AtomicReferenceFieldUpdater
			.newUpdater(Node.class, NodeState.class, "state");
	private volatile NodeState state = NodeState.LOOKING;
	private NodeEventManager nodeEventManager;

	public static Node getNode(NodeInfo nodeInfo) {
		if (null == single) {
			synchronized (Node.class) {
				if (null == single) {
					single = new Node(nodeInfo.getClusterName(), nodeInfo.getName(), nodeInfo.getHost(),
							nodeInfo.getPort(), nodeInfo.getVersion());
				}
			}

		}
		return single;
	}

	public Node(String clusterName, String name, String host, int port, long version) {
		super(clusterName + "-" + name);
		this.clusterName = clusterName;
		this.name = name;
		this.host = host;
		this.port = port;
		this.version = new AtomicLong(version);
		nodeEventManager = new NodeEventManagerImpl();
	}

	public String getClusterName() {
		return clusterName;
	}

	public String getNodeName() {
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

	public NodeState getNodeState() {
		return state;
	}

	public boolean isLooking() {
		return NodeState.LOOKING == ATOMIC_STATE.get(this);
	}

	public void looking() {
		ATOMIC_STATE.set(this, NodeState.LOOKING);
		addNodeEvent(new NodeEvent(NodeEventType.BECOME_LOOKING, nodeInfo()));
	}

	public boolean isMaster() {
		return NodeState.MASTER == ATOMIC_STATE.get(this);
	}

	public void master() {
		ATOMIC_STATE.set(this, NodeState.MASTER);
		if (isSlave()) {
			addNodeEvent(new NodeEvent(NodeEventType.RUNTIME_BECAOME_MASTER, nodeInfo()));
		} else {
			addNodeEvent(new NodeEvent(NodeEventType.INIT_BECOME_MASTER, nodeInfo()));
		}

	}

	public boolean isSlave() {
		return NodeState.SLAVE == ATOMIC_STATE.get(this);
	}

	public void slave() {
		ATOMIC_STATE.set(this, NodeState.SLAVE);
		addNodeEvent(new NodeEvent(NodeEventType.BECOME_SLAVE, nodeInfo()));
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

	public NodeEventManager getNodeEventManager() {
		return nodeEventManager;
	}

	@Override
	public boolean addNodeEvent(NodeEvent nodeEvent) {
		return nodeEventManager.addNodeEvent(nodeEvent);
	}

	@Override
	public void registerNodeEventListen(NodeEventType event, NodeEventListen nodeListen) {
		nodeEventManager.registerNodeEventListen(event, nodeListen);
	}

	@Override
	public void unregisterNodeEventListen(NodeEventType event, NodeEventListen nodeListen) {
		nodeEventManager.unregisterNodeEventListen(event, nodeListen);
	}

	@Override
	protected void doStart() {
		nodeEventManager.start();
	}

	@Override
	protected void doStop() {
		nodeEventManager.stop();
	}

}
