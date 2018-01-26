package com.six.node_manager.role.protocol;

import com.six.node_manager.role.SlaveNodeRole;

/**
*@author:MG01867
*@date:2018年1月26日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class SlaveNodeRoleProtocolImpl implements SlaveNodeRoleProtocol{
	
	private SlaveNodeRole slaveNodeRole;
	
	public SlaveNodeRoleProtocolImpl(SlaveNodeRole slaveNodeRole) {
		this.slaveNodeRole=slaveNodeRole;
	}

	@Override
	public 	void syn() {
		slaveNodeRole.syn();
	}

}
