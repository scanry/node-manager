package com.six.node_manager.role;

import com.six.node_manager.NodeInfo;

/**
 * @author:MG01867
 * @date:2018年4月2日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class AbstractNodeRoleService implements NodeRoleService {

	private NodeRole nodeRole;

	public AbstractNodeRoleService(NodeRole nodeRole) {
		this.nodeRole = nodeRole;
	}

	@Override
	public NodeInfo getNodeInfo() {
		return nodeRole.getNode().nodeInfo();
	}
}
