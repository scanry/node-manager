package com.six.node_manager;

/**
 * @author sixliu
 * @date 2018年1月17日
 * @email 359852326@qq.com
 * @Description 服务接口
 */
public interface Service {

	/**
	 * 服务状态
	 * @author Administrator
	 *
	 */
	public enum State {

		INIT, START, STOP;
	}

	/**
	 * 获取服务名称
	 * @return 
	 */
	String getName();

	/**
	 * 获取服务状态
	 * @return
	 */
	State getState();

	/**
	 * 启动服务
	 */
	void start();
	/**
	 * 停止服务
	 */
	void stop();
}
