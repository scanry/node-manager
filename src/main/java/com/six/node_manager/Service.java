package com.six.node_manager;

import java.util.concurrent.Executor;

/**
 * @author sixliu
 * @date 2018年1月17日
 * @email 359852326@qq.com
 * @Description 服务接口
 */
public interface Service {

	/**
	 * 服务状态
	 * 
	 * @author Administrator
	 *
	 */
	public enum State {

		INIT, START, STOP, FAILED;
	}

	/**
	 * 获取服务名称
	 * 
	 * @return
	 */
	String getName();

	/**
	 * 获取服务状态
	 * 
	 * @return
	 */
	State getState();
	
	boolean isRunning();


	/**
	 * 启动服务
	 */
	void start();

	/**
	 * 停止服务
	 */
	void stop();
	
	void addListener(Listener listener, Executor executor);

	interface Listener {
		
		void starting(State from);

		void stopping(State from);

		void failed(State from, Exception failure);
	}
}
