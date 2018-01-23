package com.six.node_manager.discovery;

import com.six.node_manager.NodeDiscovery;
import com.six.node_manager.NodeInfo;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class MasterNodeProtocolImpl extends AbstactNodeProtocol
		implements MasterNodeDiscoveryProtocol{

	public MasterNodeProtocolImpl(NodeDiscovery nodeDiscovery) {
		super(nodeDiscovery);
	}

	@Override
	public void join(NodeInfo info) {
		getNodeDiscovery().join(info);
	}

	@Override
	public void leave(String nodeName) {
		getNodeDiscovery().leave(nodeName);
	}
}
