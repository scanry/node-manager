package com.six.node_manager.protocol.impl;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.protocol.SlaveNodeProtocol;

/**   
 * @author sixliu   
 * @date   2018年1月12日 
 * @email  359852326@qq.com  
 * @Description 
 */
public class SlaveNodeProtocolImpl implements SlaveNodeProtocol{

	@Override
	public long ping() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getNodeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClusterName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void receiveMasterProposal(NodeInfo nodeInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public NodeInfo getMasterNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeInfo getNewestLocalNode() {
		// TODO Auto-generated method stub
		return null;
	}

}

