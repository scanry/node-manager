package com.six.node_manager.role.master;

import com.six.node_manager.NodeResource;
import com.six.node_manager.role.NodeRoleService;

/**
*@author:MG01867
*@date:2018年1月26日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public interface MasterNodeRoleService extends NodeRoleService{


	void heartbeat(NodeResource nodeResource);
}
