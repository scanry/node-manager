package com.six.node_manager.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeResource;

/**
 * @author sixliu
 * @date 2018年1月23日
 * @email 359852326@qq.com
 * @Description
 */
public class ClusterNodes {

	@FunctionalInterface
	public static interface MoreThanHalfProcess {
		void process();
	}

	private Node localNode;
	private Map<String, NodeInfo> needDiscoveryNodeInfos;
	private final int configNodeSize;
	// 加入的slave节点信息
	private Map<String, NodeInfo> joinSlaveNodeInfos = new HashMap<>();
	// 加入的slave节点信息
	private Map<String, NodeResource> hearbeatNodeResources = new TreeMap<>();
	// 丢失的slave节点信息
	private Map<String, NodeInfo> missSlaveNodeInfos = new HashMap<>();

	public ClusterNodes(Node localNode, Map<String, NodeInfo> needDiscoveryNodeInfos) {
		this.localNode = localNode;
		if (null != needDiscoveryNodeInfos) {
			this.needDiscoveryNodeInfos = new HashMap<>(needDiscoveryNodeInfos.size());
			for (Map.Entry<String, NodeInfo> entry : needDiscoveryNodeInfos.entrySet()) {
				this.needDiscoveryNodeInfos.put(entry.getKey(), entry.getValue().copy());
			}
		} else {
			this.needDiscoveryNodeInfos = Collections.emptyMap();
		}
		this.configNodeSize = this.needDiscoveryNodeInfos.size();
	}

	public Node getLocalNode() {
		return localNode;
	}

	public NodeInfo getLocalNodeInfo() {
		return localNode.nodeInfo();
	}

	public int configNodeSize() {
		return needDiscoveryNodeInfos.size();
	}

	public boolean isMember(String nodeName) {
		return needDiscoveryNodeInfos.containsKey(nodeName);
	}

	public void forEachNeedDiscoveryNodeInfos(BiConsumer<String, NodeInfo> action) {
		synchronized (needDiscoveryNodeInfos) {
			needDiscoveryNodeInfos.forEach(action);
		}
	}

	public int activitySlaveSize() {
		synchronized (hearbeatNodeResources) {
			return hearbeatNodeResources.size();
		}
	}

	public int joinSlaveSize() {
		synchronized (joinSlaveNodeInfos) {
			return joinSlaveNodeInfos.size();
		}
	}

	public int missSlaveSize() {
		synchronized (missSlaveNodeInfos) {
			return missSlaveNodeInfos.size();
		}
	}

	public int allSlaveSize() {
		return joinSlaveSize() + missSlaveSize();
	}

	public NodeInfo getJoinSlaveNodeInfos(String nodeName) {
		synchronized (joinSlaveNodeInfos) {
			return joinSlaveNodeInfos.get(nodeName);
		}
	}

	public void addJoinSlaveNodeInfos(NodeInfo nodeInfo) {
		synchronized (joinSlaveNodeInfos) {
			joinSlaveNodeInfos.put(nodeInfo.getName(), nodeInfo);
		}
	}

	public NodeInfo removeJoinSlaveNodeInfos(String nodeName) {
		synchronized (joinSlaveNodeInfos) {
			return joinSlaveNodeInfos.remove(nodeName);
		}
	}

	public NodeResource getHearbeatNodeResources(String nodeName) {
		synchronized (hearbeatNodeResources) {
			return hearbeatNodeResources.get(nodeName);
		}
	}

	public void addHearbeatNodeResources(NodeResource nodeResource) {
		synchronized (hearbeatNodeResources) {
			hearbeatNodeResources.put(nodeResource.getNodeName(), nodeResource);
		}
	}

	public NodeResource removeHearbeatNodeResources(String nodeName) {
		synchronized (hearbeatNodeResources) {
			return hearbeatNodeResources.remove(nodeName);
		}
	}

	public NodeInfo getMissSlaveNodeInfos(String nodeName) {
		synchronized (missSlaveNodeInfos) {
			return missSlaveNodeInfos.get(nodeName);
		}
	}

	public void addMissSlaveNodeInfos(NodeInfo nodeInfo) {
		synchronized (missSlaveNodeInfos) {
			missSlaveNodeInfos.put(nodeInfo.getName(), nodeInfo);
		}
	}

	public NodeInfo removeMissSlaveNodeInfos(String nodeName) {
		synchronized (missSlaveNodeInfos) {
			return missSlaveNodeInfos.remove(nodeName);
		}
	}

	public void forEachJoinSlaveNodeInfos(BiConsumer<String, NodeInfo> action) {
		synchronized (joinSlaveNodeInfos) {
			joinSlaveNodeInfos.forEach(action);
		}
	}

	public void forEachHearbeatNodeResources(BiConsumer<String, NodeResource> action) {
		synchronized (hearbeatNodeResources) {
			hearbeatNodeResources.forEach(action);
		}
	}

	public void forEachMissSlaveNodeInfos(BiConsumer<String, NodeInfo> action) {
		synchronized (missSlaveNodeInfos) {
			missSlaveNodeInfos.forEach(action);
		}
	}

	public void moreThanHalfProcess(int votes, MoreThanHalfProcess process) {
		if (votes > configNodeSize / 2 + 1) {
			process.process();
		}
	}

	public void noMoreThanHalfProcess(int votes, MoreThanHalfProcess process) {
		moreThanHalfProcess(configNodeSize - votes, process);
	}

	public boolean isMoreThanHalfProcess(int votes) {
		return votes > configNodeSize / 2;
	}
}
