package com.six.node_manager.role;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.core.AbstractService;
import com.six.node_manager.core.ClusterNodes;
import com.six.node_manager.core.Node;
import com.six.node_manager.discovery.AbstractNodeDiscovery;
/**
 * @author sixliu
 * @date 2018年1月22日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractNodeRole extends AbstractService implements NodeRole{

	private static Logger log = LoggerFactory.getLogger(AbstractNodeRole.class);
	protected Node node;
	private NodeInfo master;
	private ClusterNodes clusterNodes; 
	private AbstractNodeDiscovery nodeDiscovery;
	private NodeProtocolManager nodeProtocolManager;
	private Thread workThread;
	private long workInterval;

	public AbstractNodeRole(String name, Node node, NodeInfo master,ClusterNodes clusterNodes, AbstractNodeDiscovery nodeDiscovery,
			NodeProtocolManager nodeProtocolManager,long workInterval) {
		super(name);
		Objects.requireNonNull(node);
		Objects.requireNonNull(master);
		Objects.requireNonNull(clusterNodes);
		Objects.requireNonNull(nodeDiscovery);
		Objects.requireNonNull(nodeProtocolManager);
		this.node = node;
		this.master = master;
		this.clusterNodes=clusterNodes;
		this.nodeDiscovery = nodeDiscovery;
		this.nodeProtocolManager = nodeProtocolManager;
		this.workThread = new Thread(() -> {
			work();
		}, "NodeDiscovery-heartbeat-to-master-thread");
		this.workThread.setDaemon(true);
		this.workInterval = workInterval;
	}

	public void work() {
		while (isRunning()) {
			if (checkState()) {
				try {
					doWork();
				} catch (Exception e) {
					log.error("node role work exception", e);
				}
			}

			try {
				Thread.sleep(workInterval);
			} catch (Exception e) {
				// ignore
			}
		}
	}

	protected abstract boolean checkState();

	protected abstract void doWork();

	protected void doStart() {
		this.workThread.start();
	}

	protected void doStop() {
		workThread.interrupt();
	}

	@Override
	public final Node getNode() {
		return node;
	}

	@Override
	public final NodeInfo getMaster() {
		return master;
	}

	protected ClusterNodes getClusterNodes() {
		return clusterNodes;
	}

	protected AbstractNodeDiscovery getNodeDiscovery() {
		return nodeDiscovery;
	}

	protected NodeProtocolManager getNodeProtocolManager() {
		return nodeProtocolManager;
	}
}
