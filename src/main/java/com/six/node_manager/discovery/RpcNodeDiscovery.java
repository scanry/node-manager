package com.six.node_manager.discovery;

import java.util.List;

import com.six.node_manager.NodeEventType;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeManager;
import com.six.node_manager.protocol.MasterNodeProtocol;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class RpcNodeDiscovery extends AbstractNodeDiscovery {

	public RpcNodeDiscovery(NodeInfo localNodeInfo, List<NodeInfo> needDiscoveryNodeInfos, NodeManager nodeManager) {
		super(localNodeInfo, needDiscoveryNodeInfos, nodeManager);
	}

	@Override
	public NodeInfo getMasterNodeInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NodeInfo> getSlaveNodInfos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeInfo election() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void doStop() {}

	@Override
	public void broadcastLocalNodeInfo() {
		
	}

	@Override
	public void unbroadcastLocalNodeInfo() {
		
	}

	@Override
	protected void doStart() {
		broadcastLocalNodeInfo();
		NodeInfo masterNodeInfo = getMasterNodeInfo();
		if (null == masterNodeInfo) {
			masterNodeInfo = election();
			afterElection(masterNodeInfo);
		}
	}

	private void afterElection(NodeInfo masterNodeInfo) {
		NodeInfo localNodeInfo = getLocalNodeInfo();
		if (!localNodeInfo.equals(masterNodeInfo)) {
			getLocalNode().slave();
			MasterNodeProtocol masterNodeProtocol = getNodeManager().lookupNodeRpcProtocol(masterNodeInfo,
					MasterNodeProtocol.class);
			masterNodeProtocol.join(localNodeInfo);
			getNodeManager().getNodeEventManager().happen(NodeEventType.BECAOME_SLAVE, localNodeInfo);
		} else {
			getLocalNode().master();
			getNodeManager().getNodeEventManager().happen(NodeEventType.INIT_BECAOME_MASTER, localNodeInfo);
		}
	}
}
