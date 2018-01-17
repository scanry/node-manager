package com.six.node_manager;

/**
 * @author sixliu
 * @date 2017年12月21日
 * @email 359852326@qq.com
 * @Description
 */
@FunctionalInterface
public interface NodeEventListen {

	void listen(NodeEvent nodeEvent);
}
