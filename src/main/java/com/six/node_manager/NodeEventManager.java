package com.six.node_manager;

/**   
 * @author sixliu   
 * @date   2018年1月17日 
 * @email  359852326@qq.com  
 * @Description 
 */
public interface NodeEventManager {

	void registerNodeEventListen(NodeEvent event, NodeEventListen nodeListen);

	void unregisterNodeEventListen(NodeEvent event, NodeEventListen nodeListen);
}

