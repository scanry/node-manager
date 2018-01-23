package com.six.node_manager.role;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeEvent;
import com.six.node_manager.NodeEventType;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.discovery.AbstractNodeDiscovery;
import com.six.node_manager.discovery.SlaveNodeDiscoveryProtocol;
import com.six.node_manager.core.ClusterNodes;

/**
 * @author sixliu
 * @date 2018年1月22日
 * @email 359852326@qq.com
 * @Description
 */
public class MasterNodeRole extends AbstractNodeRole implements NodeRole {

	static Logger log = LoggerFactory.getLogger(MasterNodeRole.class);
	// 从节点向主节点心跳间隔
	private long heartbeatInterval;
	// 允许从节点向主节点心跳异常次数
	private int allowHeartbeatErrCount;
	private ClusterNodes clusterNodes;
	private List<String> tempMissNodeNames;

	public MasterNodeRole(NodeInfo master, AbstractNodeDiscovery nodeDiscovery,
			NodeProtocolManager nodeProtocolManager, ClusterNodes clusterNodes, long heartbeatInterval,
			int allowHeartbeatErrCount) {
		super("master-node-role",master,clusterNodes, nodeDiscovery, nodeProtocolManager, heartbeatInterval);
		this.clusterNodes = clusterNodes;
		this.heartbeatInterval = heartbeatInterval;
		this.allowHeartbeatErrCount = allowHeartbeatErrCount;
		this.tempMissNodeNames = new LinkedList<>();
	}

	@Override
	protected boolean checkState() {
		return getNode().isMaster();
	}

	@Override
	protected void doWork() {
		long now = System.currentTimeMillis();
		long allowHeartbeatInterval = allowHeartbeatErrCount * heartbeatInterval;
		doTempMissNodeNames();// 存在并发修改的问题
		clusterNodes.forEachHearbeatNodeResources((nodeName, item) -> {
			if ((now - item.getLastHeartbeatTime()) > allowHeartbeatInterval) {
				NodeInfo nodeInfo = clusterNodes.getJoinSlaveNodeInfos(nodeName);
				if (null != nodeInfo) {
					// 如果检查slave节点上次心跳时间距离现在大于允许的最大间隔时间的话
					SlaveNodeDiscoveryProtocol slaveNodeDiscoveryProtocol = getNodeProtocolManager()
							.lookupNodeRpcProtocol(nodeInfo, SlaveNodeDiscoveryProtocol.class, result -> {
								if (!result.isSuccessed()) {
									tempMissNodeNames.add(nodeName);
									getNode().addNodeEvent(new NodeEvent(NodeEventType.MISS_SLAVE, nodeInfo));
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
		Set<NodeInfo> slaveNodes = getNodeDiscovery().getSlaveNodInfos();
		int successedCount = 0;
		for (NodeInfo slaveNode : slaveNodes) {
			writer.write(slaveNode);
			successedCount++;
		}
		if (successedCount >= slaveNodes.size() / 2 + 1) {
			// 只要大于n/2+1个就认为成功
		}
	}

	@Override
	public final void syn() {

	}
}
