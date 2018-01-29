package com.six.node_manager.role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeResource;
import com.six.node_manager.core.ClusterNodes;
import com.six.node_manager.core.SpiExtension;
import com.six.node_manager.role.protocol.MasterNodeRoleProtocol;
import com.six.node_manager.role.protocol.SlaveNodeRoleProtocol;
import com.six.node_manager.role.protocol.SlaveNodeRoleProtocolImpl;
import com.six.node_manager.NodeResourceCollect;

/**
 * @author sixliu
 * @date 2018年1月22日
 * @email 359852326@qq.com
 * @Description
 */
public class SlaveNodeRole extends AbstractNodeRole implements NodeRole {

	static Logger log = LoggerFactory.getLogger(SlaveNodeRole.class);
	private NodeResourceCollect resourceCollect = SpiExtension.getInstance().find(NodeResourceCollect.class);
	// 允许从节点向主节点心跳异常次数
	private int allowHeartbeatErrCount;
	private int heartbeatErrCount;

	public SlaveNodeRole(NodeInfo master, ClusterNodes clusterNodes, long heartbeatInterval,
			int allowHeartbeatErrCount) {
		super("slave-node-role", master, clusterNodes, heartbeatInterval, allowHeartbeatErrCount);
		this.allowHeartbeatErrCount = allowHeartbeatErrCount;
		getNodeProtocolManager().registerNodeRpcProtocol(SlaveNodeRoleProtocol.class,
				new SlaveNodeRoleProtocolImpl(this));
	}

	@Override
	public void join() {
		MasterNodeRoleProtocol masterNodeRoleProtocol = getNodeProtocolManager().lookupNodeRpcProtocol(getMaster(),
				MasterNodeRoleProtocol.class);
		masterNodeRoleProtocol.join(getNode().nodeInfo());
	}

	@Override
	protected boolean checkState() {
		return getNode().isSlave();
	}

	@Override
	protected void doWork() {
		try {
			NodeResource nodeResource = resourceCollect.collect(getNode().getName());
			MasterNodeRoleProtocol rpcNodeDiscoveryProtocol = getNodeProtocolManager()
					.lookupNodeRpcProtocol(getMaster(), MasterNodeRoleProtocol.class);
			rpcNodeDiscoveryProtocol.heartbeat(nodeResource);
		} catch (Exception e) {
			heartbeatErrCount++;
			if (heartbeatErrCount >= allowHeartbeatErrCount) {
				heartbeatErrCount = 0;
				getNode().looking();
			}
			log.warn("heartbeat to master[" + getMaster() + "] exception", e);
		}
	}

	@Override
	public void write(Writer writer) {

	}

	@Override
	public void syn() {

	}

	@Override
	public void leave() {
		MasterNodeRoleProtocol masterNodeRoleProtocol = getNodeProtocolManager().lookupNodeRpcProtocol(getMaster(),
				MasterNodeRoleProtocol.class);
		masterNodeRoleProtocol.leave(getNode().nodeInfo());
	}
}
