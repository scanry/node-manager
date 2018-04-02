package com.six.node_manager.core;

import com.six.node_manager.NodeEvent;
import com.six.node_manager.NodeEventListen;
import com.six.node_manager.NodeEventType;
import com.six.node_manager.service.Service;

/**
 * @author sixliu
 * @date 2018年1月17日
 * @email 359852326@qq.com
 * @Description
 */
public interface NodeEventManager extends Service{

	boolean addNodeEvent(NodeEvent nodeEvent);

	void registerNodeEventListen(NodeEventType event, NodeEventListen nodeListen);
}
