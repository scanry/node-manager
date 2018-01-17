package com.six.node_manager.discovery;

import java.util.List;
import java.util.Objects;

import com.six.node_manager.AbstractService;
import com.six.node_manager.NodeDiscovery;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeManager;
import com.six.node_manager.core.Node;

/**
 * @author sixliu
 * @date 2018年1月17日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractNodeDiscovery extends AbstractService implements NodeDiscovery {

	protected Node localNode;
	private NodeInfo localNodeInfo;
	private List<NodeInfo> needDiscoveryNodeInfos;
	private NodeManager nodeManager;

	public AbstractNodeDiscovery(NodeInfo localNodeInfo, List<NodeInfo> needDiscoveryNodeInfos,NodeManager nodeManager ) {
		super("nodeDiscovery");
		Objects.requireNonNull(localNodeInfo);
		Objects.requireNonNull(nodeManager);
		this.localNodeInfo = localNodeInfo;
		this.needDiscoveryNodeInfos = needDiscoveryNodeInfos;
		this.localNode = new Node(localNodeInfo.getClusterName(), localNodeInfo.getName(), localNodeInfo.getHost(),
				localNodeInfo.getPort(), localNodeInfo.getVersion());
		this.nodeManager=nodeManager;
	}

	@Override
	public final String getClusterName() {
		return localNodeInfo.getClusterName();
	}

	@Override
	public final Node getLocalNode() {
		return localNode;
	}
	
	@Override
	public final NodeInfo getLocalNodeInfo() {
		return localNodeInfo;
	}

	@Override
	public final List<NodeInfo> getNeedDiscoveryNodeInfos() {
		return needDiscoveryNodeInfos;
	}
	
	protected final NodeManager getNodeManager() {
		return nodeManager;
	}
	
	protected abstract NodeInfo election();
}
