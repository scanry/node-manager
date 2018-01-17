package com.six.node_manager.protocol.impl;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.protocol.MasterNodeProtocol;

/**   
 * @author sixliu   
 * @date   2018年1月12日 
 * @email  359852326@qq.com  
 * @Description 
 */
public class MasterNodeProtocolImpl implements MasterNodeProtocol{

	@Override
	public long ping() {
		return System.currentTimeMillis();
	}

	@Override
	public String getNodeName() {
		return null;
	}

	@Override
	public String getClusterName() {
		return null;
	}

	@Override
	public void receiveMasterProposal(NodeInfo nodeInfo) {
		
	}

	@Override
	public NodeInfo getMasterNode() {
		return null;
	}

	@Override
	public NodeInfo getNewestLocalNode() {
		return null;
	}

	@Override
	public void join(NodeInfo slaveNodeInfo) {
		
	}

	@Override
	public void leave(NodeInfo slaveNodeInfo) {
		// TODO Auto-generated method stub
		
	}

}

