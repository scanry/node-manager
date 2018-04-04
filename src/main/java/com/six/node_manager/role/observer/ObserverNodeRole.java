package com.six.node_manager.role.observer;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeResourceCollect;
import com.six.node_manager.RemoteAdapter;
import com.six.node_manager.core.ClusterNodes;
import com.six.node_manager.role.AbstractNodeRole;
import com.six.node_manager.role.NodeRole;

/**
 * @author:MG01867
 * @date:2018年4月3日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO
 */
public class ObserverNodeRole extends AbstractNodeRole implements NodeRole {

	public ObserverNodeRole(RemoteAdapter remoteAdapter, NodeResourceCollect nodeResourceCollect, NodeInfo master,
			ClusterNodes clusterNodes, long heartbeatInterval, int allowHeartbeatErrCount) {
		super(remoteAdapter, nodeResourceCollect, master, clusterNodes, heartbeatInterval, allowHeartbeatErrCount);
		// TODO Auto-generated constructor stub
	}

	@Override
	public NodeRole work() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void leave() {
		// TODO Auto-generated method stub

	}

}
