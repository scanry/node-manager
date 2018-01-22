package com.six.node_manager.discovery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeDiscovery;
import com.six.node_manager.NodeEvent;
import com.six.node_manager.NodeEventType;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.NodeResource;
import com.six.node_manager.NodeResourceCollect;
import com.six.node_manager.NodeState;
import com.six.node_manager.core.AbstractService;
import com.six.node_manager.core.Node;
import com.six.node_manager.role.LookingNodeRole;
import com.six.node_manager.role.MasterNodeRole;
import com.six.node_manager.role.NodeRole;
import com.six.node_manager.role.SlaveNodeRole;

/**
 * @author sixliu
 * @date 2018年1月17日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractNodeDiscovery extends AbstractService implements NodeDiscovery {

	private static Logger log = LoggerFactory.getLogger(AbstractNodeDiscovery.class);

	protected Node localNode;
	private volatile NodeInfo masterNodeInfo;
	private NodeProtocolManager nodeProtocolManager;
	private MasterNodeDiscoveryProtocol masterNodeDiscoveryProtocol;
	private SlaveNodeDiscoveryProtocol slaveNodeDiscoveryProtocol;
	private NodeResourceCollect nodeResourceCollect;
	// 加入的slave节点信息
	private Map<String, NodeInfo> joinSlaveNodeInfos = new ConcurrentHashMap<>();
	// 加入的slave节点信息
	private Map<String, NodeResource> hearbeatNodeResources = new TreeMap<>();
	// 丢失的slave节点信息
	private Map<String, NodeInfo> missSlaveNodeInfos = new ConcurrentHashMap<>();
	/** 选举守护线程 **/
	private Thread electionThread;
	private AtomicBoolean notifyElectionMonitor = new AtomicBoolean(false);
	private long heartbeatInterval;
	private int allowHeartbeatErrCount;
	private NodeRole nodeRole;

	public AbstractNodeDiscovery(Node node, NodeProtocolManager nodeProtocolManager,
			NodeResourceCollect nodeResourceCollect, long heartbeatInterval, int allowHeartbeatErrCount) {
		super("nodeDiscovery");
		Objects.requireNonNull(node);
		Objects.requireNonNull(nodeProtocolManager);
		Objects.requireNonNull(nodeResourceCollect);
		this.localNode = node;
		this.nodeRole = new LookingNodeRole();
		this.nodeProtocolManager = nodeProtocolManager;
		this.nodeResourceCollect = nodeResourceCollect;
		masterNodeDiscoveryProtocol = new MasterNodeProtocolImpl(this);
		slaveNodeDiscoveryProtocol = new SlaveNodeProtocolImpl(this);
		this.heartbeatInterval = heartbeatInterval;
		this.allowHeartbeatErrCount = allowHeartbeatErrCount;
		this.electionThread = new Thread(() -> {
			election();
		}, "NodeDiscovery-election-thread");
		this.electionThread.setDaemon(true);
	}

	@Override
	public final String getClusterName() {
		return localNode.getClusterName();
	}

	@Override
	public final String getLocalNodeName() {
		return localNode.getNodeName();
	}

	@Override
	public final NodeState getNodeState() {
		return localNode.getNodeState();
	}

	@Override
	public final NodeInfo getLocalNodeInfo() {
		return localNode.nodeInfo();
	}

	@Override
	public final NodeRole getNodeRole() {
		return nodeRole;
	}

	@Override
	public final NodeInfo getMasterNodeInfo() {
		return null != masterNodeInfo ? masterNodeInfo.copy() : null;
	}

	@Override
	public final Set<NodeInfo> getSlaveNodInfos() {
		List<NodeInfo> joinList = listJoin();
		Set<NodeInfo> copySet = new HashSet<>(joinList.size());
		for (NodeInfo nodeInfo : joinList) {
			copySet.add(nodeInfo.copy());
		}
		return copySet;
	}

	@Override
	public List<NodeInfo> listJoin() {
		List<NodeInfo> list = new ArrayList<>(joinSlaveNodeInfos.size());
		for (NodeInfo nodeInfo : joinSlaveNodeInfos.values()) {
			list.add(nodeInfo.copy());
		}
		return list;
	}

	@Override
	public final void join(NodeInfo nodeInfo) {
		if (null != nodeInfo) {
			if (missSlaveNodeInfos.containsKey(nodeInfo.getName())) {
				// TODO 丢失后再次join
			}
			joinSlaveNodeInfos.put(nodeInfo.getName(), nodeInfo);
			localNode.addNodeEvent(new NodeEvent(NodeEventType.SLAVE_JOIN, nodeInfo));
		} else {
			log.warn("the join's node is null");
		}
	}

	@Override
	public final void refreshJoin(NodeResource info) {
		if (null != info) {
			info.setLastHeartbeatTime(System.currentTimeMillis());
			hearbeatNodeResources.put(info.getNodeName(), info);
		} else {
			log.warn("the heartbeat's info is null");
		}
	}

	@Override
	public final void leave(NodeInfo Info) {
		if (null != Info && null == joinSlaveNodeInfos.remove(Info.getName())) {
			log.warn("the node[" + Info.getName() + "] didn't join");
		} else {
			localNode.addNodeEvent(new NodeEvent(NodeEventType.MISS_SLAVE, Info));
		}
	}

	@Override
	public final boolean isHealthy() {
		return !localNode.isLooking();
	}

	@Override
	protected final void doStart() {
		this.localNode.registerNodeEventListen(NodeEventType.BECOME_LOOKING, node -> {
			log.warn("node[" + node + "] was looking and notify to election");
			notifyElection();
		});
		nodeProtocolManager.registerNodeRpcProtocol(MasterNodeDiscoveryProtocol.class, masterNodeDiscoveryProtocol);
		nodeProtocolManager.registerNodeRpcProtocol(SlaveNodeDiscoveryProtocol.class, slaveNodeDiscoveryProtocol);
		electionThread.start();
	}

	@Override
	protected final void doStop() {
		MasterNodeDiscoveryProtocol masterNodeProtocol = nodeProtocolManager.lookupNodeRpcProtocol(getMasterNodeInfo(),
				MasterNodeDiscoveryProtocol.class);
		masterNodeProtocol.leave(getLocalNodeInfo());
		close();
	}

	private void notifyElection() {
		if (notifyElectionMonitor.get()) {
			synchronized (notifyElectionMonitor) {
				notifyElectionMonitor.notify();
			}
		}
	}

	private void election() {
		while (isRunning()) {
			if (NodeState.LOOKING == localNode.getNodeState()) {
				if (null != nodeRole) {
					nodeRole.stop();
				}
				try {
					NodeInfo master = null;
					do {
						master = doElection();
					} while (null == master);
					master.setState(NodeState.MASTER);
					if (!getLocalNodeName().equals(master.getName())) {
						masterNodeInfo = master;
						localNode.slave();
						nodeRole = new SlaveNodeRole(localNode, master, this, nodeProtocolManager, nodeResourceCollect,
								heartbeatInterval, allowHeartbeatErrCount);
						MasterNodeDiscoveryProtocol masterNodeProtocol = nodeProtocolManager
								.lookupNodeRpcProtocol(master, MasterNodeDiscoveryProtocol.class);
						masterNodeProtocol.join(getLocalNodeInfo());
					} else {
						masterNodeInfo = master;
						localNode.master();
						nodeRole = new MasterNodeRole(localNode, master, this, nodeProtocolManager, nodeResourceCollect,
								joinSlaveNodeInfos, missSlaveNodeInfos, hearbeatNodeResources, heartbeatInterval,
								allowHeartbeatErrCount);
					}
					nodeRole.start();
				} catch (Exception e) {
					localNode.looking();
					log.error("nodeDiscovery election exception", e);
					continue;
				}
			}
			synchronized (notifyElectionMonitor) {
				notifyElectionMonitor.set(true);
				try {
					notifyElectionMonitor.wait();
				} catch (Exception e) {
					log.error("", e);
				}
			}

		}

	}

	protected abstract NodeInfo doElection();

	protected abstract void close();

	protected NodeProtocolManager getNodeProtocolManager() {
		return nodeProtocolManager;
	}
}
