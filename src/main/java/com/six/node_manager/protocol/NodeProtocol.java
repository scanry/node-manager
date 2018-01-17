package com.six.node_manager.protocol;

import com.six.node_manager.NodeInfo;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description 节点间通信协议
 */
public interface NodeProtocol {
	/**
	 * 返回接收到消息的时间
	 * 
	 * @return
	 */
	long ping();

	/**
	 * 获取节点名称
	 * 
	 * @return
	 */
	String getNodeName();

	/**
	 * 获取集群名称
	 * 
	 * @return
	 */
	String getClusterName();

	/**
	 * 接收master提议
	 * 
	 * @param nodeInfo
	 */
	void receiveMasterProposal(NodeInfo nodeInfo);

	/**
	 * 获取节点上的master
	 * 
	 * @return
	 */
	NodeInfo getMasterNode();

	/**
	 * 获取最新节点信息
	 * 
	 * @return
	 */
	NodeInfo getNewestLocalNode();
}
