package com.six.node_manager.core;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import com.six.dove.remote.AsyCallback;
import com.six.dove.rpc.client.DoveClient;
import com.six.dove.rpc.server.DoveServer;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeProtocolManager;
import com.six.node_manager.service.AbstractService;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2018年1月17日 下午9:09:43 类说明
 */
public class NodeProtocolManagerImpl extends AbstractService implements NodeProtocolManager {

	private DoveServer server;
	private DoveClient client;

	public NodeProtocolManagerImpl(String localHost, int listenPort) {
		super("NodeProtocolManager");
		server = new DoveServer(localHost, listenPort);
		client = new DoveClient();
	}

	@Override
	public <T> void registerNodeRpcProtocol(Class<T> protocol, T instance) {
		server.register(protocol, instance);
	}

	@Override
	public <T> void registerNodeRpcProtocol(ExecutorService executorService, Class<T> protocol, T instance) {
		server.register(executorService, protocol, instance);
	}

	@Override
	public void unregisterNodeRpcProtocol(Class<?> protocol) {
		server.unregister(protocol);
	}

	@Override
	public <T> T lookupNodeRpcProtocol(NodeInfo node, Class<?> protocol) {
		Objects.requireNonNull(node);
		return client.lookupService(node.getHost(), node.getPort(), protocol);
	}

	@Override
	public <T> T lookupNodeRpcProtocol(NodeInfo node, Class<?> protocol, AsyCallback callback) {
		Objects.requireNonNull(node);
		return client.lookupService(node.getHost(), node.getPort(), protocol, callback);
	}

	@Override
	protected void doStart() {
		server.start();
	}

	@Override
	protected void doStop() {
		if (null != client) {
			client.stop();
		}
		if (null != server) {
			server.stop();
		}
	}
}
