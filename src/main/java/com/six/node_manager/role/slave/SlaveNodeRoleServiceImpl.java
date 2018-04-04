package com.six.node_manager.role.slave;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.role.AbstractNodeRoleService;

/**
*@author:MG01867
*@date:2018年4月2日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class SlaveNodeRoleServiceImpl extends AbstractNodeRoleService<SlaveNodeRole> implements SlaveNodeRoleService{

	
	
	public SlaveNodeRoleServiceImpl(SlaveNodeRole slaveNodeRole) {
		super(slaveNodeRole);
	}

	@Override
	public void join(NodeInfo info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void leave(NodeInfo nodeInfo) {
		// TODO Auto-generated method stub
		
	}
	
}
