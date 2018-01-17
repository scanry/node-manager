package com.six.node_manager;

/**
 * @author sixliu
 * @date 2018年1月17日
 * @email 359852326@qq.com
 * @Description
 */
public interface Service {

	public enum State {

		INIT, START, STOP;
	}

	String getName();

	State getState();

	void start();

	void stop();
}
