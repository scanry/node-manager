package com.six.node_manager;

/**
 * @author sixliu
 * @date 2017年12月21日
 * @email 359852326@qq.com
 * @Description
 */
public enum NodeStatus {
	
	/** 未参加选举前的状态 **/
	LOOKING,
	/** 参加选举后成为master的状态 **/
	MASTER,
	/** 参加选举后成为slave的状态 **/
	SLAVE,
	/** OBSERVER状态,通过配置配，不参与选举 **/
	OBSERVER;
}
