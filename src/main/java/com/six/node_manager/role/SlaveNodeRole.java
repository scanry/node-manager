package com.six.node_manager.role;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.NodeResource;
import com.six.node_manager.core.ClusterNodes;
import com.six.node_manager.core.Node;
import com.six.node_manager.discovery.AbstractNodeDiscovery;
import com.six.node_manager.discovery.SlaveNodeDiscoveryProtocol;

/**
 * @author sixliu
 * @date 2018年1月22日
 * @email 359852326@qq.com
 * @Description
 */
public class SlaveNodeRole extends AbstractNodeRole implements NodeRole {

	static Logger log = LoggerFactory.getLogger(SlaveNodeRole.class);
	// 允许从节点向主节点心跳异常次数
	private int allowHeartbeatErrCount;
	private int heartbeatErrCount;
	private AtomicBoolean masterOk = new AtomicBoolean(true);

	public SlaveNodeRole(Node node, NodeInfo master, ClusterNodes clusterNodes, AbstractNodeDiscovery nodeDiscovery,
			NodeProtocolManager nodeProtocolManager, long heartbeatInterval, int allowHeartbeatErrCount) {
		super("slave-node-role", node, master, clusterNodes, nodeDiscovery, nodeProtocolManager, heartbeatInterval);
		this.allowHeartbeatErrCount = allowHeartbeatErrCount;
		nodeProtocolManager.registerNodeRpcProtocol(SlaveNodeRoleProtocol.class, new SlaveNodeRoleProtocolImpl(this));
	}

	@Override
	protected boolean checkState() {
		return getNode().isSlave();
	}

	@Override
	protected void doWork() {
		try {
			NodeResource nodeResource = getNode().collect(getNode().getNodeName());
			SlaveNodeDiscoveryProtocol rpcNodeDiscoveryProtocol = getNodeProtocolManager()
					.lookupNodeRpcProtocol(getMaster(), SlaveNodeDiscoveryProtocol.class);
			rpcNodeDiscoveryProtocol.heartbeat(nodeResource);
		} catch (Exception e) {
			heartbeatErrCount++;
			if (heartbeatErrCount >= allowHeartbeatErrCount || !masterOk.get()) {
				heartbeatErrCount = 0;
				masterOk.set(false);
				getClusterNodes().noMoreThanHalfProcess(checkMasterIsOk(), () -> {
					getNode().looking();
				});
			}
			log.warn("heartbeat to master[" + getMaster() + "] exception", e);
		}
	}

	public int checkMasterIsOk() {
		AtomicInteger masterIsOk = new AtomicInteger(0);
		getClusterNodes().forEachNeedDiscoveryNodeInfos((nodeName, nodeInfo) -> {
			SlaveNodeRoleProtocol slaveNodeRoleProtocol = getNodeProtocolManager().lookupNodeRpcProtocol(nodeInfo,
					SlaveNodeRoleProtocol.class);
			try {
				if (slaveNodeRoleProtocol.masterIsOk()) {
					masterIsOk.incrementAndGet();
				}
			} catch (Exception e) {
				log.error("", e);
			}
		});
		return masterIsOk.get();
	}

	public boolean masterIsOk() {
		return masterOk.get();
	}

	@Override
	public void write(Writer writer) {

	}

	@Override
	public void syn() {

	}
}
