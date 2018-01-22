package com.six.node_manager.role;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeEvent;
import com.six.node_manager.NodeEventType;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.NodeResource;
import com.six.node_manager.NodeResourceCollect;
import com.six.node_manager.core.Node;
import com.six.node_manager.discovery.AbstractNodeDiscovery;
import com.six.node_manager.discovery.SlaveNodeDiscoveryProtocol;

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
	// 加入的slave节点信息
	private Map<String, NodeInfo> joinSlaveNodeInfos = new ConcurrentHashMap<>();
	// 丢失的slave节点信息
	private Map<String, NodeInfo> missSlaveNodeInfos = new ConcurrentHashMap<>();
	// 加入的slave节点资源信息
	private Map<String, NodeResource> hearbeatNodeResources = new TreeMap<>();

	public MasterNodeRole(Node node, NodeInfo master, AbstractNodeDiscovery nodeDiscovery,
			NodeProtocolManager nodeProtocolManager, NodeResourceCollect nodeResourceCollect,
			Map<String, NodeInfo> joinSlaveNodeInfos, Map<String, NodeInfo> missSlaveNodeInfos,
			Map<String, NodeResource> hearbeatNodeResources, long heartbeatInterval, int allowHeartbeatErrCount) {
		super("master-node-role", node, master, nodeDiscovery, nodeProtocolManager, nodeResourceCollect,
				heartbeatInterval);
		this.joinSlaveNodeInfos = joinSlaveNodeInfos;
		this.missSlaveNodeInfos = missSlaveNodeInfos;
		this.hearbeatNodeResources = hearbeatNodeResources;
		this.heartbeatInterval = heartbeatInterval;
		this.allowHeartbeatErrCount = allowHeartbeatErrCount;
	}

	@Override
	protected boolean checkState() {
		return getNode().isMaster();
	}

	@Override
	protected void doWork() {
		long now = System.currentTimeMillis();
		long allowHeartbeatInterval = allowHeartbeatErrCount * heartbeatInterval;
		for (NodeResource item : hearbeatNodeResources.values()) {
			if ((now - item.getLastHeartbeatTime()) > allowHeartbeatInterval) {
				NodeInfo nodeInfo = joinSlaveNodeInfos.get(item.getNodeName());
				if (null != nodeInfo) {
					// 如果检查slave节点上次心跳时间距离现在大于允许的最大间隔时间的话
					SlaveNodeDiscoveryProtocol slaveNodeDiscoveryProtocol = getNodeProtocolManager()
							.lookupNodeRpcProtocol(nodeInfo, SlaveNodeDiscoveryProtocol.class, result -> {
								if (!result.isSuccessed()) {
									missSlaveNodeInfos.put(item.getNodeName(), nodeInfo);
									getNode().addNodeEvent(new NodeEvent(NodeEventType.MISS_SLAVE, nodeInfo));
									// 如果检查有一半从节点异常处理思考
									if (missSlaveNodeInfos.size() > joinSlaveNodeInfos.size() / 2 + 1) {
										getNode().looking();
									}
								}
							});
					slaveNodeDiscoveryProtocol.ping();
				} else {
					log.warn("this node[" + item.getNodeName() + "] was leaved");
				}
			}
		}
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
