package com.six.node_manager.role;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.core.ClusterNodes;
import com.six.node_manager.service.Service;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2018年1月19日 下午7:36:22 类说明 节点角色
 */
public interface NodeRole extends Service {

	Node getNode();

	NodeInfo getMaster();
	
	ClusterNodes getClusterNodes();
	
	void join();
	
	void work();

	void write(Writer writer);

	void syn();
	
	void leave();

	@FunctionalInterface
	public interface Writer {
		void write(NodeInfo nodeInfo);
	}
}
