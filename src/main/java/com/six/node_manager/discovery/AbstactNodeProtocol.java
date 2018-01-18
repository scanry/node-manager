package com.six.node_manager.discovery;

import java.util.Objects;

import com.six.node_manager.NodeInfo;

/**
 * @author sixliu
 * @date 2018年1月18日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstactNodeProtocol implements NodeDiscoveryProtocol {

	private AbstractNodeDiscovery abstractNodeDiscovery;

	public AbstactNodeProtocol(AbstractNodeDiscovery abstractNodeDiscovery) {
		Objects.requireNonNull(abstractNodeDiscovery);
		this.abstractNodeDiscovery = abstractNodeDiscovery;
	}

	@Override
	public long ping() {
		return System.currentTimeMillis();
	}

	@Override
	public String getNodeName() {
		return abstractNodeDiscovery.getLocalNodeInfo().getName();
	}

	@Override
	public String getClusterName() {
		return abstractNodeDiscovery.getLocalNodeInfo().getClusterName();
	}

	@Override
	public NodeInfo getMasterNode() {
		return abstractNodeDiscovery.getMasterNodeInfo();
	}

	@Override
	public NodeInfo getNewestLocalNode() {
		return abstractNodeDiscovery.getLocalNodeInfo();
	}

	protected AbstractNodeDiscovery getAbstractNodeDiscovery() {
		return abstractNodeDiscovery;
	}

}
