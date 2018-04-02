package com.six.node_manager.role;

import com.six.node_manager.NodeInfo;

/**
 * @author:MG01867
 * @date:2018年3月23日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe 节点角色服务接口
 */
public interface NodeRoleService {

	/**
	 * 获取最新的节点信息
	 * 
	 * @return
	 */
	NodeInfo getNodeInfo();
}
