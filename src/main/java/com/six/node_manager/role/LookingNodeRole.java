package com.six.node_manager.role;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.core.ClusterNodes;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2018年1月19日 下午7:38:11 类说明
 */

public class LookingNodeRole extends AbstractNodeRole implements NodeRole{

	public LookingNodeRole(NodeInfo master, ClusterNodes clusterNodes, long workInterval,int allowHeartbeatErrCount) {
		super("looking-node-role", master, clusterNodes, workInterval,allowHeartbeatErrCount);
	}

	@Override
	public void join() {
		
	}

	@Override
	public void write(Writer writer) {
		
	}

	@Override
	public void syn() {
		
	}

	@Override
	public void leave() {
		
	}

	@Override
	protected boolean checkState() {
		return false;
	}

	@Override
	protected void doWork() {
		
	}
	
}
