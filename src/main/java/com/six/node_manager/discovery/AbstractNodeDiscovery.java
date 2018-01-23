package com.six.node_manager.discovery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeDiscovery;
import com.six.node_manager.NodeEvent;
import com.six.node_manager.NodeEventType;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.NodeResource;
import com.six.node_manager.NodeState;
import com.six.node_manager.core.AbstractService;
import com.six.node_manager.core.ClusterNodes;
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

	private volatile NodeInfo masterNodeInfo;
	private NodeProtocolManager nodeProtocolManager;
	private MasterNodeDiscoveryProtocol masterNodeDiscoveryProtocol;
	private SlaveNodeDiscoveryProtocol slaveNodeDiscoveryProtocol;
	// 进群节点容器
	private ClusterNodes clusterNodes;
	/** 选举守护线程 **/
	private Thread electionThread;
	private AtomicBoolean notifyElectionMonitor = new AtomicBoolean(false);
	private long heartbeatInterval;
	private int allowHeartbeatErrCount;
	private NodeRole nodeRole;

	public AbstractNodeDiscovery(ClusterNodes clusterNodes, NodeProtocolManager nodeProtocolManager,
			long heartbeatInterval, int allowHeartbeatErrCount) {
		super("nodeDiscovery");
		Objects.requireNonNull(clusterNodes);
		Objects.requireNonNull(nodeProtocolManager);
		this.clusterNodes = clusterNodes;
		this.nodeRole = new LookingNodeRole();
		this.nodeProtocolManager = nodeProtocolManager;
		masterNodeDiscoveryProtocol = new MasterNodeProtocolImpl(this);
		slaveNodeDiscoveryProtocol = new SlaveNodeProtocolImpl(this);
		this.heartbeatInterval = heartbeatInterval;
		this.allowHeartbeatErrCount = allowHeartbeatErrCount;
		this.electionThread = new Thread(() -> {
			election();
		}, "NodeDiscovery-election-thread");
		this.electionThread.setDaemon(true);
	}

	protected ClusterNodes getClusterNodes() {
		return clusterNodes;
	}

	@Override
	public final String getClusterName() {
		return clusterNodes.getLocalNode().getClusterName();
	}

	@Override
	public final String getLocalNodeName() {
		return clusterNodes.getLocalNode().getNodeName();
	}

	@Override
	public final NodeState getNodeState() {
		return clusterNodes.getLocalNode().getNodeState();
	}

	@Override
	public final NodeInfo getLocalNodeInfo() {
		return clusterNodes.getLocalNode().nodeInfo();
	}

	protected Node getNode() {
		return clusterNodes.getLocalNode();
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
		return new HashSet<>(joinList);
	}

	@Override
	public List<NodeInfo> listJoin() {
		List<NodeInfo> list = new ArrayList<>(clusterNodes.joinSlaveSize());
		clusterNodes.forEachJoinSlaveNodeInfos((nodeName, nodeInfo) -> {
			list.add(nodeInfo.copy());
		});
		return list;
	}

	@Override
	public final void join(NodeInfo nodeInfo) {
		if (null != nodeInfo) {
			if (null != clusterNodes.removeMissSlaveNodeInfos(nodeInfo.getName())) {
				log.info("miss slave node[" + nodeInfo + "] join");
			}
			clusterNodes.addJoinSlaveNodeInfos(nodeInfo);
			clusterNodes.getLocalNode().addNodeEvent(new NodeEvent(NodeEventType.SLAVE_JOIN, nodeInfo));
		} else {
			log.warn("the join's node is null");
		}
	}

	@Override
	public final void refreshJoin(NodeResource info) {
		if (null != info) {
			info.setLastHeartbeatTime(System.currentTimeMillis());
			clusterNodes.addHearbeatNodeResources(info);
		} else {
			log.warn("the heartbeat's info is null");
		}
	}

	@Override
	public final void leave(String nodeName) {
		NodeInfo info = null;
		if (null != nodeName && info == clusterNodes.removeJoinSlaveNodeInfos(nodeName)) {
			log.warn("the node[" + nodeName + "] didn't join");
		} else {
			clusterNodes.getLocalNode().addNodeEvent(new NodeEvent(NodeEventType.MISS_SLAVE, info));
		}
	}

	@Override
	public final boolean isHealthy() {
		return !clusterNodes.getLocalNode().isLooking();
	}

	@Override
	protected final void doStart() {
		this.clusterNodes.getLocalNode().registerNodeEventListen(NodeEventType.BECOME_LOOKING, node -> {
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
		masterNodeProtocol.leave(getLocalNodeName());
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
			if (NodeState.LOOKING == clusterNodes.getLocalNode().getNodeState()) {
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
						clusterNodes.getLocalNode().slave();
						nodeRole = new SlaveNodeRole(master, clusterNodes, this, nodeProtocolManager, heartbeatInterval,
								allowHeartbeatErrCount);
						MasterNodeDiscoveryProtocol masterNodeProtocol = nodeProtocolManager
								.lookupNodeRpcProtocol(master, MasterNodeDiscoveryProtocol.class);
						masterNodeProtocol.join(getLocalNodeInfo());
					} else {
						masterNodeInfo = master;
						clusterNodes.getLocalNode().master();
						nodeRole = new MasterNodeRole(master, this, nodeProtocolManager, clusterNodes,
								heartbeatInterval, allowHeartbeatErrCount);
					}
					nodeRole.start();
				} catch (Exception e) {
					clusterNodes.getLocalNode().looking();
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

	protected NodeProtocolManager getNodeProtocolManager() {
		return nodeProtocolManager;
	}
}
