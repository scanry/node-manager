package com.six.node_manager.discovery;

import com.six.node_manager.NodeDiscovery;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class SlaveNodeProtocolImpl extends AbstactNodeProtocol
		implements SlaveNodeDiscoveryProtocol {

	public SlaveNodeProtocolImpl(NodeDiscovery nodeDiscovery) {
		super(nodeDiscovery);
	}

}
