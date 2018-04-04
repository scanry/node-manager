package com.six.node_manager.role;

import com.six.node_manager.NodeInfo;

/**
 * @author:MG01867
 * @date:2018年4月2日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public abstract class AbstractNodeRoleService<N extends NodeRole> implements NodeRoleService {

	private N nodeRole;

	public AbstractNodeRoleService(N nodeRole) {
		this.nodeRole = nodeRole;
	}

	protected N getnodeRole() {
		return nodeRole;
	}
	
	@Override
	public NodeInfo getNodeInfo() {
		return nodeRole.getNode().nodeInfo();
	}
}
