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
import com.six.node_manager.NodeEventManager;
import com.six.node_manager.NodeEventType;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.NodeState;
import com.six.node_manager.core.ClusterNodes;
import com.six.node_manager.core.SpiExtension;
import com.six.node_manager.role.LookingNodeRole;
import com.six.node_manager.role.MasterNodeRole;
import com.six.node_manager.role.Node;
import com.six.node_manager.role.NodeRole;
import com.six.node_manager.role.SlaveNodeRole;
import com.six.node_manager.service.AbstractService;

/**
 * @author sixliu
 * @date 2018年1月17日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractNodeDiscovery extends AbstractService implements NodeDiscovery {

	private static Logger log = LoggerFactory.getLogger(AbstractNodeDiscovery.class);
	private NodeEventManager nodeEventManager=SpiExtension.getInstance().find(NodeEventManager.class);
	private NodeProtocolManager nodeProtocolManager=SpiExtension.getInstance().find(NodeProtocolManager.class);
	private NodeDiscoveryProtocol nodeDiscoveryProtocol;
	// 进群节点容器
	private ClusterNodes clusterNodes;
	/** 选举守护线程 **/
	private Thread electionThread;
	private AtomicBoolean notifyElectionMonitor = new AtomicBoolean(false);
	private long heartbeatInterval;
	private int allowHeartbeatErrCount;
	private volatile NodeRole nodeRole;

	public AbstractNodeDiscovery(ClusterNodes clusterNodes,
			long heartbeatInterval, int allowHeartbeatErrCount) {
		super("nodeDiscovery");
		Objects.requireNonNull(clusterNodes);
		this.clusterNodes = clusterNodes;
		this.heartbeatInterval = heartbeatInterval;
		this.allowHeartbeatErrCount = allowHeartbeatErrCount;
		this.nodeRole=new LookingNodeRole(this.clusterNodes.getLocalNodeInfo(), clusterNodes, heartbeatInterval,allowHeartbeatErrCount);
		this.electionThread = new Thread(() -> {
			election();
		}, "NodeDiscovery-election-thread");
		this.electionThread.setDaemon(true);
	}

	@Override
	protected final void doStart() {
		this.nodeDiscoveryProtocol = new NodeDiscoveryProtocolImpl(this);
		nodeProtocolManager.registerNodeRpcProtocol(NodeDiscoveryProtocol.class, nodeDiscoveryProtocol);
		nodeEventManager.registerNodeEventListen(NodeEventType.BECOME_LOOKING, node -> {
			log.warn("node[" + node + "] was looking and notify to election");
			notifyElection();
		});
		electionThread.start();
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
		return clusterNodes.getLocalNode().getName();
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
		return null != clusterNodes.getMasterInfo() ? clusterNodes.getMasterInfo().copy() : null;
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
	public final boolean isHealthy() {
		return null!=nodeRole&&!clusterNodes.getLocalNode().isLooking();
	}

	@Override
	protected final void doStop() {
		if (null != nodeRole) {
			nodeRole.leave();
		}
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
				try {
					NodeInfo master = null;
					do {
						master = doElection();
					} while (null == master);
					master.setState(NodeState.MASTER);
					clusterNodes.setMasterInfo(master);
					if (!getLocalNodeName().equals(master.getName())) {
						clusterNodes.getLocalNode().slave();
						nodeRole = new SlaveNodeRole(master,clusterNodes, heartbeatInterval,
								allowHeartbeatErrCount);
					} else {
						clusterNodes.getLocalNode().master();
						nodeRole = new MasterNodeRole(master, clusterNodes,
								heartbeatInterval, allowHeartbeatErrCount);
					}
					nodeRole.join();
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
			if (null != nodeRole) {
				nodeRole.stop();
			}

		}

	}

	protected abstract NodeInfo doElection();
}
