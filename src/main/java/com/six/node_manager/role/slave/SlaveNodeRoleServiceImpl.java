package com.six.node_manager.role.slave;

import com.six.node_manager.NodeInfo;

/**
*@author:MG01867
*@date:2018年4月2日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class SlaveNodeRoleServiceImpl implements SlaveNodeRoleService{

	
	private SlaveNodeRole slaveNodeRole;
	
	public SlaveNodeRoleServiceImpl(SlaveNodeRole slaveNodeRole) {
		this.slaveNodeRole=slaveNodeRole;
	}
	
	@Override
	public NodeInfo getNodeInfo() {
		return slaveNodeRole.getNode().nodeInfo();
	}
}
