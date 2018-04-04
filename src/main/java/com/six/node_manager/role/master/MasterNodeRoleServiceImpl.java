package com.six.node_manager.role.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.dove.rpc.annotation.DoveService;
import com.six.node_manager.NodeInfo;
import com.six.node_manager.NodeResource;
import com.six.node_manager.NodeStatus;
import com.six.node_manager.role.AbstractNodeRoleService;

/**
 * @author:MG01867
 * @date:2018年1月26日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe //TODO 注意：当节点挂掉后，是否需要将注册的rpc服务取消掉，值得思考
 */
@DoveService(protocol = MasterNodeRoleService.class)
public class MasterNodeRoleServiceImpl extends AbstractNodeRoleService<MasterNodeRole>
		implements MasterNodeRoleService {

	private static Logger log = LoggerFactory.getLogger(MasterNodeRoleServiceImpl.class);

	public MasterNodeRoleServiceImpl(MasterNodeRole masterNodeRole) {
		super(masterNodeRole);
	}

	@Override
	public final void join(NodeInfo nodeInfo) {
		if (null != nodeInfo && NodeStatus.SLAVE == nodeInfo.getState()) {
			if (null != getnodeRole().getClusterNodes().removeMissSlaveNodeInfos(nodeInfo.getName())) {
				log.info("miss slave node[" + nodeInfo + "] join");
			}
			getnodeRole().getClusterNodes().addJoinSlaveNodeInfos(nodeInfo);
		} else {
			log.warn("the join's node is null");
		}
	}

	@Override
	public final void heartbeat(NodeResource nodeResource) {
		if (null != nodeResource) {
			nodeResource.setLastHeartbeatTime(System.currentTimeMillis());
			getnodeRole().getClusterNodes().addHearbeatNodeResources(nodeResource);
		} else {
			log.warn("the heartbeat's info is null");
		}
	}

	@Override
	public final void leave(NodeInfo nodeInfo) {
		if (null != nodeInfo && NodeStatus.SLAVE == nodeInfo.getState()) {
			if (null != getnodeRole().getClusterNodes().removeJoinSlaveNodeInfos(nodeInfo.getName())) {
				log.warn("the node[" + nodeInfo + "] didn't join");
			}
		}
	}
}
