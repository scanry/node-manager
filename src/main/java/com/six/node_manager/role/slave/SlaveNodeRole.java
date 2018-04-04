package com.six.node_manager.role.slave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeResource;
import com.six.node_manager.core.ClusterNodes;
import com.six.node_manager.role.AbstractNodeRole;
import com.six.node_manager.role.NodeRole;
import com.six.node_manager.role.looking.LookingNodeRole;
import com.six.node_manager.role.master.MasterNodeRoleService;
import com.six.node_manager.NodeResourceCollect;
import com.six.node_manager.RemoteAdapter;

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
	private volatile int heartbeatErrCount;

	public SlaveNodeRole(RemoteAdapter remoteAdapter, NodeResourceCollect nodeResourceCollect, NodeInfo master,
			ClusterNodes clusterNodes, long heartbeatInterval, int allowHeartbeatErrCount) {
		super(remoteAdapter, nodeResourceCollect, master, clusterNodes, heartbeatInterval, allowHeartbeatErrCount);
		this.allowHeartbeatErrCount = allowHeartbeatErrCount;
		getRemoteAdapter().registerNodeRpcProtocol(getExecutorService(), new SlaveNodeRoleServiceImpl(this));
		MasterNodeRoleService masterNodeRoleProtocol = getRemoteAdapter().lookupNodeRpcProtocol(getMaster(),
				MasterNodeRoleService.class);
		masterNodeRoleProtocol.join(getNode().nodeInfo());
	}

	@Override
	public NodeRole work() {
		NodeRole staticNodeRole = null;
		try {
			NodeResource nodeResource = getNodeResourceCollect().collect(getNode().getName());
			MasterNodeRoleService rpcNodeDiscoveryProtocol = getRemoteAdapter().lookupNodeRpcProtocol(getMaster(),
					MasterNodeRoleService.class);
			rpcNodeDiscoveryProtocol.heartbeat(nodeResource);
			staticNodeRole = this;
		} catch (Exception e) {
			heartbeatErrCount++;
			if (heartbeatErrCount >= allowHeartbeatErrCount) {
				getNode().isLooking();
				staticNodeRole = new LookingNodeRole(getRemoteAdapter(), getNodeResourceCollect(), getNode().nodeInfo(),
						getClusterNodes(), getHeartbeatInterval(), getAllowHeartbeatErrCount());
			}
			log.warn("heartbeat to master[" + getMaster() + "] exception", e);
		}
		return staticNodeRole;
	}

	@Override
	public void leave() {
		MasterNodeRoleService masterNodeRoleProtocol = getRemoteAdapter().lookupNodeRpcProtocol(getMaster(),
				MasterNodeRoleService.class);
		masterNodeRoleProtocol.leave(getNode().nodeInfo());
	}
}
