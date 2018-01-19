package com.six.node_manager;

import java.util.Set;

/**
 * @author sixliu
 * @date 2017年12月21日
 * @email 359852326@qq.com
 * @Description
 */
public interface NodeManager extends Service {

	/**
	 * 获取当前集群名称
	 * 
	 * @return 集群名称
	 */
	String getClusterName();

	/**
	 * 获取当前本地节点信息
	 * 
	 * @return
	 */
	NodeInfo getLocalNodeInfo();

	/**
	 * 获取master节点
	 * 
	 * @return
	 */
	NodeInfo getMasterNode();

	/**
	 * 获取所有从节点
	 * 
	 * @return
	 */
	Set<NodeInfo> getSlaveNods();

	/**
	 * 获取节点事件管理
	 * 
	 * @return
	 */
	NodeEventManager getNodeEventManager();

	/**
	 * 获取节点通信协议管理
	 * 
	 * @return
	 */
	NodeProtocolManager getNodeProtocolManager();

	/**
	 * 获取分布式缓存
	 * 
	 * @param cacheName
	 *            缓存名称
	 * @return
	 */
	Cache newCache(String cacheName);

	/**
	 * 获取分布式锁
	 * 
	 * @return
	 */
	Lock newLock();

	/**
	 * 获取分布式文件系统
	 * 
	 * @return
	 */
	FileSystem getFileSystem();
}
