package com.six.node_manager.role;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.role.protocol.MasterNodeRoleProtocolImpl;
import com.six.node_manager.core.ClusterNodes;
import com.six.node_manager.discovery.protocol.NodeDiscoveryProtocol;

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

	public MasterNodeRole(NodeInfo master, ClusterNodes clusterNodes, long heartbeatInterval,
			int allowHeartbeatErrCount) {
		super("master-node-role", master, clusterNodes, heartbeatInterval, allowHeartbeatErrCount);
		this.clusterNodes = clusterNodes;
		this.tempMissNodeNames = new LinkedList<>();
		getNodeProtocolManager().registerNodeRpcProtocol(new MasterNodeRoleProtocolImpl(this));
	}

	@Override
	public void join() {
		// 不需要做任何处理，从节点做处理即可
	}

	@Override
	protected boolean checkState() {
		return getNode().isMaster();
	}

	@Override
	protected void doWork() {
		long now = System.currentTimeMillis();
		doTempMissNodeNames();// 存在并发修改的问题
		clusterNodes.forEachHearbeatNodeResources((nodeName, item) -> {
			if ((now - item.getLastHeartbeatTime()) > getAllowMaxHeartbeatInterval()) {
				NodeInfo nodeInfo = clusterNodes.getJoinSlaveNodeInfos(nodeName);
				if (null != nodeInfo) {
					// 如果检查slave节点上次心跳时间距离现在大于允许的最大间隔时间的话
					NodeDiscoveryProtocol slaveNodeDiscoveryProtocol = getNodeProtocolManager()
							.lookupNodeRpcProtocol(nodeInfo, NodeDiscoveryProtocol.class, result -> {
								if (!result.isSuccessed()) {
									tempMissNodeNames.add(nodeName);
									// 如果检查有一半从节点异常处理思考
									clusterNodes.noMoreThanHalfProcess(clusterNodes.joinSlaveSize(), () -> {
										getNode().looking();
									});
								}
							});
					slaveNodeDiscoveryProtocol.ping();
				} else {
					log.warn("this node[" + item.getNodeName() + "] was leaved");
				}
			}
		});
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
	public final void write(Writer writer) {
		// Set<NodeInfo> slaveNodes = clusterNodes.
		// int successedCount = 0;
		// for (NodeInfo slaveNode : slaveNodes) {
		// writer.write(slaveNode);
		// successedCount++;
		// }
		// if (successedCount >= slaveNodes.size() / 2 + 1) {
		// // 只要大于n/2+1个就认为成功
		// }
	}

	@Override
	public final void syn() {

	}

	@Override
	public void leave() {
		// 不需要做任何处理，从节点做处理即可
	}
}
