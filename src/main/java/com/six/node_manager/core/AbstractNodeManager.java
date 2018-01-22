package com.six.node_manager.core;

import com.six.node_manager.NodeManager;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractNodeManager extends AbstractService implements NodeManager {

	public AbstractNodeManager() {
		super("nodeManager");
	}
}
