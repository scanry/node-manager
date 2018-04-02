package com.six.node_manager.core;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeDiscovery;
import com.six.node_manager.NodeEventListen;
import com.six.node_manager.NodeEventType;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.role.NodeRole;
import com.six.node_manager.service.AbstractService;

/**
 * @author sixliu
 * @date 2018年1月17日
 * @email 359852326@qq.com
 * @Description
 */
public class NodeDiscoveryImpl extends AbstractService implements NodeDiscovery {

	static Logger log = LoggerFactory.getLogger(NodeDiscoveryImpl.class);
	/** 选举守护线程 **/
	private Thread keepaliveThread;
	private NodeEventManager nodeEventManager;
	private AtomicBoolean notifyElectionMonitor = new AtomicBoolean(false);
	private volatile NodeRole nodeRole;

	public NodeDiscoveryImpl(NodeRole initNodeRole,NodeEventManager nodeEventManager) {
		super("nodeDiscovery");
		Objects.requireNonNull(initNodeRole);
		Objects.requireNonNull(nodeEventManager);
		this.nodeEventManager = nodeEventManager;
		this.nodeRole = initNodeRole;
		this.keepaliveThread = new Thread(() -> {
			while (isRunning()) {
				nodeRole = nodeRole.work();
			}
		}, "nodeDiscovery-keepalive-thread");
		this.keepaliveThread.setDaemon(true);
	}

	@Override
	protected final void doStart() {
		keepaliveThread.start();
	}

	protected ClusterNodes getClusterNodes() {
		return nodeRole.getClusterNodes();
	}

	@Override
	public final NodeInfo getLocalNodeInfo() {
		return nodeRole.getNode().nodeInfo();
	}

	@Override
	public final NodeInfo getMasterNodeInfo() {
		return null != nodeRole.getMaster() ? nodeRole.getMaster().copy() : null;
	}

	@Override
	protected final void doStop() {
		notifyElection();
	}

	private void notifyElection() {
		if (notifyElectionMonitor.get()) {
			synchronized (notifyElectionMonitor) {
				notifyElectionMonitor.notify();
			}
		}
	}

	@Override
	public List<NodeInfo> getAllSlaveNodeInfos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NodeInfo> getOnineSlaveNodeInfos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NodeInfo> getOffineSlaveNodeInfos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerNodeEventListener(NodeEventType event, NodeEventListen nodeListen) {
		nodeEventManager.registerNodeEventListen(event, nodeListen);
	}
}
