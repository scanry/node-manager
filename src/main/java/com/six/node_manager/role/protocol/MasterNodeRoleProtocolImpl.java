package com.six.node_manager.role.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeResource;
import com.six.node_manager.role.MasterNodeRole;

/**
*@author:MG01867
*@date:2018年1月26日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class MasterNodeRoleProtocolImpl implements MasterNodeRoleProtocol{
	
	private static Logger log = LoggerFactory.getLogger(MasterNodeRoleProtocolImpl.class);
	
	private MasterNodeRole masterNodeRole;

	public MasterNodeRoleProtocolImpl(MasterNodeRole masterNodeRole) {
		this.masterNodeRole=masterNodeRole;
	}

	@Override
	public final void join(NodeInfo nodeInfo) {
		if (null != nodeInfo) {
			if (null != masterNodeRole.getClusterNodes().removeMissSlaveNodeInfos(nodeInfo.getName())) {
				log.info("miss slave node[" + nodeInfo + "] join");
			}
			masterNodeRole.getClusterNodes().addJoinSlaveNodeInfos(nodeInfo);
		} else {
			log.warn("the join's node is null");
		}
	}

	@Override
	public final void heartbeat(NodeResource nodeResource) {
		if (null != nodeResource) {
			nodeResource.setLastHeartbeatTime(System.currentTimeMillis());
			masterNodeRole.getClusterNodes().addHearbeatNodeResources(nodeResource);
		} else {
			log.warn("the heartbeat's info is null");
		}
	}

	@Override
	public final void leave(String nodeName) {
		if(null != nodeName) {
			if (null!=masterNodeRole.getClusterNodes().removeJoinSlaveNodeInfos(nodeName)) {
				log.warn("the node[" + nodeName + "] didn't join");
			}
		}
	}

}
