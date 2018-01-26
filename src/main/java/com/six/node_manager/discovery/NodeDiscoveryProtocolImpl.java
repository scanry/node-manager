package com.six.node_manager.discovery;

import java.util.Objects;

import com.six.node_manager.NodeDiscovery;
import com.six.node_manager.NodeInfo;

/**
 * @author sixliu
 * @date 2018年1月18日
 * @email 359852326@qq.com
 * @Description
 */
public class NodeDiscoveryProtocolImpl implements NodeDiscoveryProtocol{

	private NodeDiscovery nodeDiscovery;

	public NodeDiscoveryProtocolImpl(NodeDiscovery nodeDiscovery) {
		Objects.requireNonNull(nodeDiscovery);
		this.nodeDiscovery = nodeDiscovery;
	}

	@Override
	public long ping() {
		return System.currentTimeMillis();
	}

	@Override
	public String getNodeName() {
		return nodeDiscovery.getLocalNodeInfo().getName();
	}

	@Override
	public String getClusterName() {
		return nodeDiscovery.getLocalNodeInfo().getClusterName();
	}

	@Override
	public NodeInfo getMasterNode() {
		return nodeDiscovery.getMasterNodeInfo();
	}

	@Override
	public NodeInfo getNewestLocalNode() {
		return nodeDiscovery.getLocalNodeInfo();
	}

	protected NodeDiscovery getNodeDiscovery() {
		return nodeDiscovery;
	}

}
