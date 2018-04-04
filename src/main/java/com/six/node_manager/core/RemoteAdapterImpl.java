package com.six.node_manager.core;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import com.six.dove.remote.AsyCallback;
import com.six.dove.rpc.client.DoveClientImpl;
import com.six.dove.rpc.server.DoveServerImpl;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.RemoteAdapter;
import com.six.node_manager.service.AbstractService;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2018年1月17日 下午9:09:43 类说明
 */
public class RemoteAdapterImpl extends AbstractService implements RemoteAdapter {

	private DoveServerImpl server;
	private DoveClientImpl client;

	public RemoteAdapterImpl(String localHost, int listenPort) {
		super("NodeProtocolManager");
		server = new DoveServerImpl(localHost, listenPort);
		client = new DoveClientImpl();
	}


	@Override
	public void registerNodeRpcProtocol(ExecutorService executorService,Object instance) {
		server.register(instance);
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
