package com.six.node_manager.role;

/**
 * @author sixliu
 * @date 2018年1月23日
 * @email 359852326@qq.com
 * @Description
 */
public class SlaveNodeRoleProtocolImpl implements SlaveNodeRoleProtocol {

	private SlaveNodeRole slaveNodeRole;

	public SlaveNodeRoleProtocolImpl(SlaveNodeRole slaveNodeRole) {
		this.slaveNodeRole = slaveNodeRole;
	}

	@Override
	public boolean masterIsOk() {
		return slaveNodeRole.masterIsOk();
	}

}
