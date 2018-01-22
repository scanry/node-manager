package com.six.node_manager.discovery;

import com.six.node_manager.NodeInfo;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description 主节点通信协议
 */
public interface MasterNodeDiscoveryProtocol extends NodeDiscoveryProtocol{

	void join(NodeInfo info);

	void leave(NodeInfo info);
}
