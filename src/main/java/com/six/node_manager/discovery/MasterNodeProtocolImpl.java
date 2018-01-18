package com.six.node_manager.discovery;

import com.six.node_manager.NodeInfo;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class MasterNodeProtocolImpl extends AbstactNodeProtocol implements MasterNodeDiscoveryProtocol {

	public MasterNodeProtocolImpl(AbstractNodeDiscovery abstractNodeDiscovery) {
		super(abstractNodeDiscovery);
	}

	@Override
	public void join(NodeInfo slaveNodeInfo) {
		getAbstractNodeDiscovery().join(slaveNodeInfo);
	}

	@Override
	public void leave(NodeInfo slaveNodeInfo) {
		getAbstractNodeDiscovery().leave(slaveNodeInfo);
	}

}
