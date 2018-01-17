package com.six.node_manager;

/**
 * @author sixliu
 * @date 2017年12月21日
 * @email 359852326@qq.com
 * @Description 节点事件
 */
public enum NodeEventType {
	/** 集群初始化启动时成为master **/
	INIT_BECAOME_MASTER,
	/** 成为slave **/
	BECAOME_SLAVE,
	/** slave加入 **/
	SLAVE_JOIN,
	/** 丢失master **/
	MISS_MASTER,
	/** 运行期master丢失后重新选举成为master **/
	RUNTIME_BECAOME_MASTER,
	/** 丢失slave **/
	MISS_SLAVE;

}
