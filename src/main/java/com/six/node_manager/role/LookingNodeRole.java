package com.six.node_manager.role;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.core.Node;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2018年1月19日 下午7:38:11 类说明
 */

public class LookingNodeRole implements NodeRole{

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public State getState() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRunning() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public Node getNode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public NodeInfo getMaster() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void work() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(Writer writer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void syn() {
		throw new UnsupportedOperationException();
	}
}
