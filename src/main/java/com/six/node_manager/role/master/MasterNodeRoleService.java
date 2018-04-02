package com.six.node_manager.role.master;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeResource;

/**
*@author:MG01867
*@date:2018年1月26日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public interface MasterNodeRoleService {

	void join(NodeInfo info);

	void leave(NodeInfo nodeInfo);
	
	void heartbeat(NodeResource nodeResource);
}
