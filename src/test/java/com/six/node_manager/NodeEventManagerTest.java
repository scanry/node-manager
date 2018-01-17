package com.six.node_manager;

import com.six.node_manager.core.NodeEventManagerImpl;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2018年1月17日 下午11:31:23 类说明
 */
public class NodeEventManagerTest {

	public static void main(String[] args) {
		NodeEventManager nodeEventManager = new NodeEventManagerImpl();
		nodeEventManager.start();
		nodeEventManager.stop();
	}

}
