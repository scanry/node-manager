package com.six.node_manager.discovery;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
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

	private static final long DEFAULT_STATE_CHECK_INTERVAL = 3000;
	private static final int DEFAULT_HEARTBEAT_ERR_COUNT = 3;
	protected Node localNode;
	private volatile NodeInfo masterNodeInfo;
	// 加入的slave节点信息
	private Map<String, NodeInfoHeartbeat> joinSlaveNodeInfos = new ConcurrentHashMap<>();
	// 丢失的slave节点信息
	private Map<String, NodeInfoHeartbeat> missSlaveNodeInfos = new ConcurrentHashMap<>();
	private NodeProtocolManager nodeProtocolManager;
	private NodeEventManager nodeEventManager;
	private MasterNodeDiscoveryProtocol masterNodeDiscoveryProtocol;
	private SlaveNodeDiscoveryProtocol slaveNodeDiscoveryProtocol;
	/** 选举守护线程 **/
	private Thread electionThread;
	/** 向master心跳守护线程 **/
	private Thread heartbeatToMasterThread;
	private AtomicBoolean heartbeatToMasterMonitor = new AtomicBoolean(false);
	/** 检查slave心跳守护线程 **/
	private Thread checkSlaveHeartbeatThread;
	private AtomicBoolean checkSlaveHeartbeatMonitor = new AtomicBoolean(false);
	private long stateCheckInterval;
	private long heartbeatInterval;
	private long checkSlaveheartbeatInterval;
	private int allowHeartbeatErrCount;
	private int heartbeatErrCount = 0;

	public AbstractNodeDiscovery(NodeInfo localNodeInfo, NodeProtocolManager nodeProtocolManager,
			NodeEventManager nodeEventManager) {
		this(localNodeInfo, nodeProtocolManager, nodeEventManager, DEFAULT_STATE_CHECK_INTERVAL,
				DEFAULT_HEARTBEAT_ERR_COUNT);
	}

	public AbstractNodeDiscovery(NodeInfo localNodeInfo, NodeProtocolManager nodeProtocolManager,
			NodeEventManager nodeEventManager, long stateCheckInterval, int allowHeartbeatErrCount) {
		super("nodeDiscovery");
		Objects.requireNonNull(localNodeInfo);
		Objects.requireNonNull(nodeProtocolManager);
		Objects.requireNonNull(nodeEventManager);
		this.localNode = new Node(localNodeInfo.getClusterName(), localNodeInfo.getName(), localNodeInfo.getHost(),
				localNodeInfo.getPort(), localNodeInfo.getVersion());
		this.nodeProtocolManager = nodeProtocolManager;
		this.nodeEventManager = nodeEventManager;
		masterNodeDiscoveryProtocol = new MasterNodeProtocolImpl(this);
		slaveNodeDiscoveryProtocol = new SlaveNodeProtocolImpl(this);
		nodeProtocolManager.registerNodeRpcProtocol(MasterNodeDiscoveryProtocol.class, masterNodeDiscoveryProtocol);
		nodeProtocolManager.registerNodeRpcProtocol(SlaveNodeDiscoveryProtocol.class, slaveNodeDiscoveryProtocol);
		this.stateCheckInterval = stateCheckInterval;
		this.heartbeatInterval = stateCheckInterval;
		this.checkSlaveheartbeatInterval = stateCheckInterval;
		this.allowHeartbeatErrCount = allowHeartbeatErrCount;
		this.electionThread = new Thread(() -> {
			election();
		}, "NodeDiscovery-election-thread");
		this.electionThread.setDaemon(true);

		this.heartbeatToMasterThread = new Thread(() -> {
			heartbeatToMaster();
		}, "NodeDiscovery-heartbeat-to-master-thread");
		this.heartbeatToMasterThread.setDaemon(true);

		this.checkSlaveHeartbeatThread = new Thread(() -> {
			checkSlaveheartbeat();
		}, "NodeDiscovery-check slave-heartbeat-thread");
		this.checkSlaveHeartbeatThread.setDaemon(true);
	}

	@Override
	public final String getClusterName() {
		return localNode.clusterName;
	}

	@Override
	public final String getLocalNodeName() {
		return localNode.name;
	}

	@Override
	public final NodeState getNodeState() {
		return localNode.state;
	}

	@Override
	public final NodeInfo getLocalNodeInfo() {
		return localNode.NodeInfo();
	}

	@Override
	public final NodeInfo getMasterNodeInfo() {
		return null != masterNodeInfo ? masterNodeInfo.copy() : null;
	}

	@Override
	public final Set<NodeInfo> getSlaveNodInfos() {
		Set<NodeInfo> copySet = new HashSet<>(joinSlaveNodeInfos.size());
		for (NodeInfoHeartbeat nodeInfoHeartbeat : joinSlaveNodeInfos.values()) {
			copySet.add(nodeInfoHeartbeat.getNodeInfo().copy());
		}
		return copySet;
	}

	@Override
	public final boolean isHealthy() {
		return !localNode.isLooking();
	}

	@Override
	public final void join(NodeInfo slaveNodeInfo) {
		if (null != slaveNodeInfo) {
			refreshJoinNode(slaveNodeInfo);
			nodeEventManager.addNodeEvent(new NodeEvent(NodeEventType.SLAVE_JOIN, slaveNodeInfo));
		} else {
			log.warn("the join's node is null");
		}
	}

	protected final void refreshJoinNode(NodeInfo slaveNodeInfo) {
		if (null != slaveNodeInfo) {
			NodeInfoHeartbeat nodeInfoHeartbeat = joinSlaveNodeInfos.computeIfAbsent(slaveNodeInfo.getName(),
					newKey -> {
						NodeInfoHeartbeat newNodeInfoHeartbeat = new NodeInfoHeartbeat();
						newNodeInfoHeartbeat.setNodeInfo(slaveNodeInfo);
						newNodeInfoHeartbeat.setLastHeartbeatTime(System.currentTimeMillis());
						return newNodeInfoHeartbeat;
					});
			nodeInfoHeartbeat.setNodeInfo(slaveNodeInfo);
			nodeInfoHeartbeat.setLastHeartbeatTime(System.currentTimeMillis());
		} else {
			log.warn("the join's node is null");
		}
	}

	@Override
	public final void leave(NodeInfo slaveNodeInfo) {
		if (null == joinSlaveNodeInfos.remove(slaveNodeInfo.getName())) {
			log.warn("the node[" + slaveNodeInfo.getName() + "] didn't join");
		} else {
			nodeEventManager.addNodeEvent(new NodeEvent(NodeEventType.MISS_SLAVE, slaveNodeInfo));
		}
	}

	@Override
	protected final void doStart() {
		electionThread.start();
		heartbeatToMasterThread.start();
		checkSlaveHeartbeatThread.start();
	}

	@Override
	protected final void doStop() {
		MasterNodeDiscoveryProtocol masterNodeProtocol = nodeProtocolManager.lookupNodeRpcProtocol(getMasterNodeInfo(),
				MasterNodeDiscoveryProtocol.class);
		masterNodeProtocol.leave(getLocalNodeInfo());
		close();
	}

	private void election() {
		while (isRunning()) {
			switch (localNode.getState()) {
			case LOOKING:
				try {
					NodeInfo master = doElection();
					if (null != master) {
						master.setState(NodeState.MASTER);
						if (!getLocalNodeName().equals(master.getName())) {
							localNode.slave();
							masterNodeInfo = master;
							MasterNodeDiscoveryProtocol masterNodeProtocol = nodeProtocolManager
									.lookupNodeRpcProtocol(master, MasterNodeDiscoveryProtocol.class);
							NodeInfo localNodeInfo = getLocalNodeInfo();
							masterNodeProtocol.join(getLocalNodeInfo());
							nodeEventManager.addNodeEvent(new NodeEvent(NodeEventType.BECAOME_SLAVE, localNodeInfo));
						} else {
							localNode.master();
							NodeInfo initNodeInfo = masterNodeInfo;
							masterNodeInfo = master;
							if (null == initNodeInfo) {
								nodeEventManager.addNodeEvent(
										new NodeEvent(NodeEventType.INIT_BECAOME_MASTER, getLocalNodeInfo()));
							} else {
								nodeEventManager.addNodeEvent(
										new NodeEvent(NodeEventType.RUNTIME_BECAOME_MASTER, getLocalNodeInfo()));
							}
						}
					}
				} catch (Exception e) {
					log.error("nodeDiscovery election exception", e);
				}
				break;
			case MASTER:
				notifyCheckSlaveheartbeat();
				break;
			case SLAVE:
				notifyHeartbeatToMaster();
				break;
			default:
				break;
			}
			try {
				Thread.sleep(stateCheckInterval);
			} catch (Exception e) {
				log.error("", e);
			}
		}

	}

	protected abstract NodeInfo doElection();

	private void notifyHeartbeatToMaster() {
		if (!heartbeatToMasterMonitor.get()) {
			synchronized (heartbeatToMasterMonitor) {
				heartbeatToMasterMonitor.set(true);
				heartbeatToMasterMonitor.notify();
			}
		}
	}

	private void heartbeatToMaster() {
		while (isRunning()) {
			if (null != masterNodeInfo && NodeState.SLAVE == localNode.getState()) {
				try {
					doHeartbeat(masterNodeInfo, getLocalNodeInfo());
				} catch (Exception e) {
					heartbeatErrCount++;
					if (heartbeatErrCount >= allowHeartbeatErrCount) {
						heartbeatErrCount = 0;
						localNode.looking();
					}
				}
				try {
					Thread.sleep(heartbeatInterval);
				} catch (Exception e) {
					log.error("", e);
				}
			} else {
				heartbeatToMasterMonitor.set(false);
				synchronized (heartbeatToMasterMonitor) {
					try {
						heartbeatToMasterMonitor.wait();
					} catch (InterruptedException e) {
						log.error("", e);
					}
				}
			}
		}
	}

	private void notifyCheckSlaveheartbeat() {
		if (!checkSlaveHeartbeatMonitor.get()) {
			synchronized (checkSlaveHeartbeatMonitor) {
				checkSlaveHeartbeatMonitor.set(true);
				checkSlaveHeartbeatMonitor.notify();
			}
		}
	}

	private final void checkSlaveheartbeat() {
		while (isRunning()) {
			if (NodeState.MASTER == localNode.getState()) {
				long now = System.currentTimeMillis();
				long allowHeartbeatInterval = allowHeartbeatErrCount * heartbeatInterval;
				for (NodeInfoHeartbeat item : joinSlaveNodeInfos.values()) {
					if ((now - item.getLastHeartbeatTime()) > allowHeartbeatInterval) {
						// 如果检查slave节点上次心跳时间距离现在大于允许的最大间隔时间的话
						SlaveNodeDiscoveryProtocol slaveNodeDiscoveryProtocol = getNodeProtocolManager()
								.lookupNodeRpcProtocol(item.getNodeInfo(), SlaveNodeDiscoveryProtocol.class, result -> {
									if (!result.isSuccessed()) {
										missSlaveNodeInfos.put(item.getNodeInfo().getName(), item);
										//TODO如果检查有一半从节点异常处理思考
									}
								});
						slaveNodeDiscoveryProtocol.ping();
					}
				}
				try {
					Thread.sleep(checkSlaveheartbeatInterval);
				} catch (Exception e) {
					log.error("", e);
				}
			} else {
				checkSlaveHeartbeatMonitor.set(false);
				synchronized (checkSlaveHeartbeatMonitor) {
					try {
						checkSlaveHeartbeatMonitor.wait();
					} catch (InterruptedException e) {
						log.error("", e);
					}
				}
			}
		}
	}

	protected abstract void doHeartbeat(NodeInfo masterNodeInfo, NodeInfo localNodeInfo);

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

		NodeState getState() {
			return state;
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
