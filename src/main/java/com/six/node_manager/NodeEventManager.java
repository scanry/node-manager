package com.six.node_manager;

/**
 * @author sixliu
 * @date 2018年1月17日
 * @email 359852326@qq.com
 * @Description
 */
public interface NodeEventManager extends Service{

	boolean addNodeEvent(NodeEvent nodeEvent);

	void registerNodeEventListen(NodeEventType event, NodeEventListen nodeListen);

	void unregisterNodeEventListen(NodeEventType event, NodeEventListen nodeListen);
}
