package com.six.node_manager.discovery.rpc;

import com.six.node_manager.NodeInfo;

/**   
 * @author sixliu   
 * @date   2018年1月18日 
 * @email  359852326@qq.com  
 * @Description 
 */
public interface RpcNodeDiscoveryProtocol {

	String getName();
	
	MasterProposal sendMasterProposal(MasterProposal masterProposal);
	
	void heartbeat(NodeInfo nodeInfo);
}

