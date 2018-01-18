package com.six.node_manager.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.AbstractService;
import com.six.node_manager.NodeDiscovery;
import com.six.node_manager.NodeEvent;
import com.six.node_manager.NodeEventManager;
import com.six.node_manager.NodeEventType;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.NodeState;

/**
 * @author sixliu
 * @date 2018年1月17日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractNodeDiscovery extends AbstractService implements NodeDiscovery {

	private static Logger log = LoggerFactory.getLogger(AbstractNodeDiscovery.class);

	protected Node localNode;
	private NodeInfo localNodeInfo;
	private List<NodeInfo> needDiscoveryNodeInfos;
	private volatile NodeInfo masterNodeInfo;
	private Map<String, NodeInfo> slaveNodeInfos = new ConcurrentHashMap<>();
	private NodeProtocolManager nodeProtocolManager;
	private NodeEventManager nodeEventManager;
	private Thread heartbeatThread;
	private long heartbeatInterval;
	private int allowHeartbeatErrCount;
	private long electionInterval;
	private Thread electionThread;
	private Object electionMonitor = new Object();
	private MasterNodeDiscoveryProtocol masterNodeDiscoveryProtocol;
	private SlaveNodeDiscoveryProtocol slaveNodeDiscoveryProtocol;

	public AbstractNodeDiscovery(NodeInfo localNodeInfo, List<NodeInfo> needDiscoveryNodeInfos,
			NodeProtocolManager nodeProtocolManager, NodeEventManager nodeEventManager, long heartbeatInterval,
			int allowHeartbeatErrCount, long electionInterval) {
		super("nodeDiscovery");
		Objects.requireNonNull(localNodeInfo);
		Objects.requireNonNull(nodeProtocolManager);
		Objects.requireNonNull(nodeEventManager);
		this.localNodeInfo = localNodeInfo.copy();
		if (null != needDiscoveryNodeInfos) {
			this.needDiscoveryNodeInfos = new ArrayList<>(needDiscoveryNodeInfos.size());
			for (NodeInfo needDiscoveryNodeInfo : needDiscoveryNodeInfos) {
				this.needDiscoveryNodeInfos.add(needDiscoveryNodeInfo.copy());
			}
		} else {
			this.needDiscoveryNodeInfos = Collections.emptyList();
		}
		this.localNode = new Node(localNodeInfo.getClusterName(), localNodeInfo.getName(), localNodeInfo.getHost(),
				localNodeInfo.getPort(), localNodeInfo.getVersion());
		this.nodeProtocolManager = nodeProtocolManager;
		this.nodeEventManager = nodeEventManager;
		this.heartbeatInterval = heartbeatInterval;
		this.heartbeatThread = new Thread(() -> {
			heartbeat();
		}, "NodeDiscovery-heartbeat-Thread");
		this.heartbeatThread.setDaemon(true);
		this.electionThread = new Thread(() -> {
			election();
		}, "NodeDiscovery-election-Thread");
		this.electionThread.setDaemon(true);
		masterNodeDiscoveryProtocol = new MasterNodeProtocolImpl(this);
		slaveNodeDiscoveryProtocol = new SlaveNodeProtocolImpl(this);
		nodeProtocolManager.registerNodeRpcProtocol(MasterNodeDiscoveryProtocol.class, masterNodeDiscoveryProtocol);
		nodeProtocolManager.registerNodeRpcProtocol(SlaveNodeDiscoveryProtocol.class, slaveNodeDiscoveryProtocol);
	}

	@Override
	public final String getClusterName() {
		return localNodeInfo.getClusterName();
	}

	@Override
	public final NodeInfo getLocalNodeInfo() {
		return localNodeInfo.copy();
	}

	@Override
	public final NodeInfo getMasterNodeInfo() {
		return null != masterNodeInfo ? masterNodeInfo.copy() : null;
	}

	@Override
	public final List<NodeInfo> getSlaveNodInfos() {
		List<NodeInfo> copyList = new ArrayList<>(slaveNodeInfos.size());
		for (NodeInfo nodeInfo : slaveNodeInfos.values()) {
			copyList.add(nodeInfo.copy());
		}
		return copyList;
	}

	@Override
	public final List<NodeInfo> getNeedDiscoveryNodeInfos() {
		List<NodeInfo> copyList = new ArrayList<>(needDiscoveryNodeInfos.size());
		for (NodeInfo nodeInfo : needDiscoveryNodeInfos) {
			copyList.add(nodeInfo.copy());
		}
		return copyList;
	}

	@Override
	public final boolean isHealthy() {
		return !localNode.isLooking();
	}

	@Override
	public final void join(NodeInfo slaveNodeInfo) {
		if (null != slaveNodeInfo) {
			slaveNodeInfos.put(slaveNodeInfo.getName(), slaveNodeInfo);
			nodeEventManager.addNodeEvent(new NodeEvent(NodeEventType.SLAVE_JOIN, slaveNodeInfo));
		} else {
			log.warn("the join's node is null");
		}
	}

	@Override
	public final void leave(NodeInfo slaveNodeInfo) {
		if (null == slaveNodeInfos.remove(slaveNodeInfo.getName())) {
			log.warn("the node[" + slaveNodeInfo.getName() + "] didn't join");
		} else {
			nodeEventManager.addNodeEvent(new NodeEvent(NodeEventType.MISS_SLAVE, slaveNodeInfo));
		}
	}

	@Override
	protected final void doStart() {
		heartbeatThread.start();
		electionThread.start();
	}

	@Override
	protected final void doStop() {
		MasterNodeDiscoveryProtocol masterNodeProtocol = nodeProtocolManager.lookupNodeRpcProtocol(getMasterNodeInfo(),
				MasterNodeDiscoveryProtocol.class);
		masterNodeProtocol.leave(localNodeInfo.copy());
		close();
	}

	private void election() {
		synchronized (electionMonitor) {
			while (isRunning()) {
				try {
					NodeInfo master = null;
					do {
						master = doElection();
						if (null == master) {
							Thread.sleep(electionInterval);
						}
					} while (null == master);
					NodeInfo localNodeInfo = getLocalNodeInfo();
					if (!localNodeInfo.equals(master)) {
						MasterNodeDiscoveryProtocol masterNodeProtocol = nodeProtocolManager
								.lookupNodeRpcProtocol(master, MasterNodeDiscoveryProtocol.class);
						masterNodeProtocol.join(localNodeInfo);
						masterNodeInfo = master;
						localNode.slave();
						nodeEventManager.addNodeEvent(new NodeEvent(NodeEventType.BECAOME_SLAVE, localNodeInfo));
					} else {
						localNode.master();
						NodeInfo initNodeInfo = masterNodeInfo;
						masterNodeInfo = localNodeInfo;
						if (null == initNodeInfo) {
							nodeEventManager
									.addNodeEvent(new NodeEvent(NodeEventType.INIT_BECAOME_MASTER, localNodeInfo));
						} else {
							nodeEventManager
									.addNodeEvent(new NodeEvent(NodeEventType.RUNTIME_BECAOME_MASTER, localNodeInfo));
						}
					}
				} catch (Exception e) {
					log.error("nodeDiscovery election exception", e);
				}
				try {
					electionMonitor.wait();
				} catch (Exception e) {
				}
			}
		}
	}

	protected abstract NodeInfo doElection();

	protected final void heartbeat() {
		int heartbeatErrCount = 0;
		while (isRunning()) {
			try {
				doHeartbeat();
			} catch (Exception e) {
				heartbeatErrCount++;
				if (heartbeatErrCount >= allowHeartbeatErrCount) {
					localNode.looking();
					synchronized (electionMonitor) {
						electionMonitor.notifyAll();
					}
				}
			}
			try {
				Thread.sleep(heartbeatInterval);
			} catch (InterruptedException e) {
			}
		}
	}

	protected abstract void doHeartbeat();

	protected abstract void close();

	protected NodeProtocolManager getNodeProtocolManager() {
		return nodeProtocolManager;
	}

	class Node {
		private final String clusterName;
		private final String name;
		private final String host;
		private final int port;
		private AtomicLong version;
		private AtomicReferenceFieldUpdater<Node, NodeState> ATOMIC_STATE = AtomicReferenceFieldUpdater
				.newUpdater(Node.class, NodeState.class, "state");
		private volatile NodeState state = NodeState.LOOKING;

		private Node(String clusterName, String name, String host, int port, long version) {
			this.clusterName = clusterName;
			this.name = name;
			this.host = host;
			this.port = port;
			this.version = new AtomicLong(version);
		}

		boolean isLooking() {
			return NodeState.LOOKING == ATOMIC_STATE.get(this);
		}

		void looking() {
			ATOMIC_STATE.set(this, NodeState.LOOKING);
		}

		boolean isMaster() {
			return NodeState.MASTER == ATOMIC_STATE.get(this);
		}

		void master() {
			ATOMIC_STATE.set(this, NodeState.MASTER);
		}

		boolean isSlave() {
			return NodeState.SLAVE == ATOMIC_STATE.get(this);
		}

		void slave() {
			ATOMIC_STATE.set(this, NodeState.SLAVE);
		}

		long getVersion() {
			return version.get();
		}

		void incVersion() {
			version.incrementAndGet();
		}

		NodeInfo NodeInfo() {
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
}
