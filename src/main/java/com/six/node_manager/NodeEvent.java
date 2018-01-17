package com.six.node_manager;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2018年1月17日 下午11:04:31 类说明
 */
public class NodeEvent {

	private NodeEventType type;
	private NodeInfo nodeInfo;

	public NodeEvent(NodeEventType type, NodeInfo nodeInfo) {
		this.type = type;
		this.nodeInfo = nodeInfo;
	}

	public NodeEventType getType() {
		return type;
	}

	public NodeInfo getNodeInfo() {
		return nodeInfo;
	}

	@Override
	public String toString() {
		return "NodeEvent [type=" + type + ", nodeInfo=" + nodeInfo + "]";
	}
}
