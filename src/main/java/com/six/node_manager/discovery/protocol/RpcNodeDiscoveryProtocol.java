package com.six.node_manager.discovery.protocol;

import com.six.node_manager.discovery.MasterProposal;

/**   
 * @author sixliu   
 * @date   2018年1月18日 
 * @email  359852326@qq.com  
 * @Description 
 */
public interface RpcNodeDiscoveryProtocol{

	MasterProposal sendMasterProposal(MasterProposal masterProposal);
}

