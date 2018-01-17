package com.six.node_manager.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.six.node_manager.NodeEvent;
import com.six.node_manager.NodeEventListen;
import com.six.node_manager.NodeEventManager;
import com.six.node_manager.NodeInfo;

/**
 * @author sixliu
 * @date 2018年1月17日
 * @email 359852326@qq.com
 * @Description
 */
public class NodeEventManagerImpl implements NodeEventManager {

	private Map<NodeEvent, Set<NodeEventListen>> nodeEventListens = new ConcurrentHashMap<>();

	protected void happen(NodeEvent event,NodeInfo nodeInfo) {
		
	}

	@Override
	public void registerNodeEventListen(NodeEvent event, NodeEventListen nodeListen) {
		Objects.requireNonNull(event);
		Objects.requireNonNull(nodeListen);
		nodeEventListens.computeIfAbsent(event, newKey -> new HashSet<>()).add(nodeListen);
	}

	@Override
	public void unregisterNodeEventListen(NodeEvent event, NodeEventListen nodeListen) {
		Set<NodeEventListen> set = nodeEventListens.get(event);
		if (null != set) {
			set.remove(nodeListen);
		}
	}

}
