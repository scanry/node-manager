package com.six.node_manager.role.master;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeResourceCollect;
import com.six.node_manager.RemoteAdapter;
import com.six.node_manager.role.AbstractNodeRole;
import com.six.node_manager.role.NodeRole;
import com.six.node_manager.role.looking.LookingNodeRole;
import com.six.node_manager.core.ClusterNodes;

/**
 * @author sixliu
 * @date 2018年1月22日
 * @email 359852326@qq.com
 * @Description
 */
public class MasterNodeRole extends AbstractNodeRole implements NodeRole {

	static Logger log = LoggerFactory.getLogger(MasterNodeRole.class);
	private ClusterNodes clusterNodes;
	private List<String> tempMissNodeNames;

	public MasterNodeRole(RemoteAdapter remoteAdapter, NodeResourceCollect nodeResourceCollect, NodeInfo master,
			ClusterNodes clusterNodes, long heartbeatInterval, int allowHeartbeatErrCount) {
		super(remoteAdapter, nodeResourceCollect, master, clusterNodes, heartbeatInterval, allowHeartbeatErrCount);
		this.clusterNodes = clusterNodes;
		this.tempMissNodeNames = new LinkedList<>();
		getRemoteAdapter().registerNodeRpcProtocol(getExecutorService(),new MasterNodeRoleServiceImpl(this));
	}

	@Override
	public NodeRole work() {
		long now = System.currentTimeMillis();
		doTempMissNodeNames();// 存在并发修改的问题
		clusterNodes.forEachHearbeatNodeResources((nodeName, item) -> {
			if ((now - item.getLastHeartbeatTime()) > getAllowMaxHeartbeatInterval()) {
				tempMissNodeNames.add(nodeName);
				// 如果检查有一半从节点异常处理思考
				clusterNodes.noMoreThanHalfProcess(clusterNodes.joinSlaveSize(), () -> {
					getNode().looking();
				});
			}
		});
		NodeRole staticNodeRole = null;
		if (getNode().isLooking()) {
			staticNodeRole = new LookingNodeRole(getRemoteAdapter(), getNodeResourceCollect(), getNode().nodeInfo(),
					getClusterNodes(), getHeartbeatInterval(), getAllowHeartbeatErrCount());
		} else {
			staticNodeRole = this;
		}
		return staticNodeRole;
	}

	private void doTempMissNodeNames() {
		NodeInfo nodeInfo = null;
		for (String nodeName : tempMissNodeNames) {
			nodeInfo = clusterNodes.removeJoinSlaveNodeInfos(nodeName);
			clusterNodes.addMissSlaveNodeInfos(nodeInfo);
			clusterNodes.removeHearbeatNodeResources(nodeName);
		}
		tempMissNodeNames.clear();
	}

	@Override
	public void leave() {
		clusterNodes.forEachNeedDiscoveryNodeInfos((nodeName, item) -> {
			MasterNodeRoleService masterNodeRoleProtocol = getRemoteAdapter().lookupNodeRpcProtocol(item,
					MasterNodeRoleService.class,null);
			masterNodeRoleProtocol.leave(getNode().nodeInfo());
		});

	}
}
