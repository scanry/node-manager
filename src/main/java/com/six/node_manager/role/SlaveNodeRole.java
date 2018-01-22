package com.six.node_manager.role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class SlaveNodeRole extends AbstractNodeRole implements NodeRole {
	
	static Logger log = LoggerFactory.getLogger(SlaveNodeRole.class);
	// 允许从节点向主节点心跳异常次数
	private int allowHeartbeatErrCount;
	private int heartbeatErrCount;

	public SlaveNodeRole(Node node, NodeInfo master, AbstractNodeDiscovery nodeDiscovery,
			NodeProtocolManager nodeProtocolManager,
			NodeResourceCollect nodeResourceCollect, long heartbeatInterval, int allowHeartbeatErrCount) {
		super("slave-node-role", node, master, nodeDiscovery, nodeProtocolManager,
				nodeResourceCollect, heartbeatInterval);
		this.allowHeartbeatErrCount = allowHeartbeatErrCount;
	}

	@Override
	protected boolean checkState() {
		return getNode().isSlave();
	}

	@Override
	protected void doWork() {
		try {
			NodeResource nodeResource = getNodeResourceCollect().collect();
			SlaveNodeDiscoveryProtocol rpcNodeDiscoveryProtocol = getNodeProtocolManager()
					.lookupNodeRpcProtocol(getMaster(), SlaveNodeDiscoveryProtocol.class);
			rpcNodeDiscoveryProtocol.heartbeat(nodeResource);
		} catch (Exception e) {
			heartbeatErrCount++;
			if (heartbeatErrCount >= allowHeartbeatErrCount) {
				heartbeatErrCount = 0;
				getNode().looking();
				throw new RuntimeException("heartbeat to master exception", e);
			}else {
				log.warn("heartbeat to master["+getMaster() +"] exception", e);
			}
		}
	}

	@Override
	public void write(Writer writer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void syn() {
		// TODO Auto-generated method stub

	}
}
