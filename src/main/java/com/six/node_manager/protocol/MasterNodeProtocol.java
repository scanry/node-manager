package com.six.node_manager.protocol;

import com.six.node_manager.NodeInfo;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description 主节点通信协议
 */
public interface MasterNodeProtocol extends NodeProtocol{

	void join(NodeInfo slaveNodeInfo);

	void leave(NodeInfo slaveNodeInfo);
}
