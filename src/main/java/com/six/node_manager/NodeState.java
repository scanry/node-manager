package com.six.node_manager;

/**
 * @author sixliu
 * @date 2017年12月21日
 * @email 359852326@qq.com
 * @Description
 */
public enum NodeState {
	/** 未参加选举前的状态 **/
	LOOKING,
	/** 参加选举后成为master的状态 **/
	MASTER,
	/** 参加选举后成为slave的状态 **/
	SLAVE;
}
