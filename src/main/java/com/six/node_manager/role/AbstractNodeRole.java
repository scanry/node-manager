package com.six.node_manager.role;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.core.ClusterNodes;
import com.six.node_manager.core.SpiExtension;
import com.six.node_manager.service.AbstractService;

/**
 * @author sixliu
 * @date 2018年1月22日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractNodeRole extends AbstractService implements NodeRole {

	private static Logger log = LoggerFactory.getLogger(AbstractNodeRole.class);
	private NodeInfo master;
	private ClusterNodes clusterNodes;
	private NodeProtocolManager nodeProtocolManager=SpiExtension.getInstance().find(NodeProtocolManager.class);
	private Thread workThread;
	// 从节点向主节点心跳间隔
	private long heartbeatInterval;
	// 允许从节点向主节点心跳异常次数
	private int allowHeartbeatErrCount;
	private long allowMaxHeartbeatInterval;

	public AbstractNodeRole(String name,NodeInfo master, ClusterNodes clusterNodes,long heartbeatInterval,int allowHeartbeatErrCount) {
		super(name);
		Objects.requireNonNull(master);
		Objects.requireNonNull(clusterNodes);
		this.master = master;
		this.clusterNodes=clusterNodes;
		this.heartbeatInterval = heartbeatInterval;
		this.allowHeartbeatErrCount = allowHeartbeatErrCount;
		this.allowMaxHeartbeatInterval=allowHeartbeatErrCount*heartbeatInterval;
		this.workThread = new Thread(() -> {
			work();
		}, "NodeDiscovery-heartbeat-to-master-thread");
		this.workThread.setDaemon(true);
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
				Thread.sleep(heartbeatInterval);
			} catch (Exception e) {
				// ignore
			}
		}
	}

	protected abstract boolean checkState();

	protected abstract void doWork();

	@Override
	protected void doStart() {
		this.workThread.start();
	}

	@Override
	protected void doStop() {
		workThread.interrupt();
	}

	@Override
	public final Node getNode() {
		return clusterNodes.getLocalNode();
	}

	@Override
	public final NodeInfo getMaster() {
		return master;
	}

	@Override
	public final ClusterNodes getClusterNodes() {
		return clusterNodes;
	}

	protected NodeProtocolManager getNodeProtocolManager() {
		return nodeProtocolManager;
	}
	
	protected long getHeartbeatInterval() {
		return heartbeatInterval;
	}
	
	protected int getAllowHeartbeatErrCount() {
		return allowHeartbeatErrCount;
	}
	
	protected long getAllowMaxHeartbeatInterval() {
		return allowMaxHeartbeatInterval;
	}
}
