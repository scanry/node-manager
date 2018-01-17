package com.six.node_manager.lock;

import com.six.node_manager.Lock;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2018年1月17日 下午8:24:51 类说明
 */
public class ClusterLock implements Lock {

	private transient volatile Thread exclusiveOwnerThread;

	@Override
	public void lock() {
		Thread currentThread = Thread.currentThread();
		if (currentThread == exclusiveOwnerThread) {

		}
	}

	@Override
	public void unlock() {
		
	}

}
