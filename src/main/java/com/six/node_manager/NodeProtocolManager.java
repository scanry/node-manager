package com.six.node_manager;

import java.util.concurrent.ExecutorService;

import com.six.node_manager.service.Service;

import six.com.rpc.AsyCallback;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2018年1月17日 下午8:15:23 类说明 节点通信协议管理接口
 */
public interface NodeProtocolManager extends Service{

	/**
	 * 注册本地协议服务
	 * 
	 * @param protocol
	 *            服务协议
	 */
	<T>void registerNodeRpcProtocol(Class<T> protocol,T instance);

	/**
	 * 注册本地协议服务
	 * 
	 * @param executorService
	 *            执行业务逻辑线程池
	 * @param protocol
	 *            服务协议
	 */
	<T>void registerNodeRpcProtocol(ExecutorService executorService, Class<T> protocol,T instance);

	/**
	 * 取消注册服务协议
	 * 
	 * @param protocol
	 */
	void unregisterNodeRpcProtocol(Class<?> protocol);

	/**
	 * 寻找指定节点上的协议服务
	 * 
	 * @param node
	 *            目标节点
	 * @param protocol
	 *            服务协议
	 * @return 返回一个同步调用服务实例
	 */
	<T> T lookupNodeRpcProtocol(NodeInfo node, Class<?> protocol);

	/**
	 * 寻找指定节点上的协议服务
	 * 
	 * @param node
	 *            目标节点
	 * @param protocol
	 *            服务协议
	 * @param callback
	 *            回调
	 * @return 返回一个异步调用服务实例
	 */
	<T> T lookupNodeRpcProtocol(NodeInfo node, Class<?> protocol, AsyCallback callback);
}
