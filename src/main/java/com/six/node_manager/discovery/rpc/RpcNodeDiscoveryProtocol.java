package com.six.node_manager.discovery.rpc;


/**   
 * @author sixliu   
 * @date   2018年1月18日 
 * @email  359852326@qq.com  
 * @Description 
 */
public interface RpcNodeDiscoveryProtocol {

	void sendMasterProposal(MasterProposal masterProposal);
}

