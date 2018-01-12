package com.six.node_manager.core;

import java.util.Objects;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeManager;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public abstract class AbstractNodeManager implements NodeManager {

	protected Node localNode;

	public AbstractNodeManager(NodeInfo localNodeInfo) {
		Objects.requireNonNull(localNodeInfo);
		this.localNode = new Node(localNodeInfo.getClusterName(), localNodeInfo.getName(), localNodeInfo.getHost(),
				localNodeInfo.getPort(), localNodeInfo.getVersion());
	}

	protected final Node getLocalNode() {
		return localNode;
	}
}
