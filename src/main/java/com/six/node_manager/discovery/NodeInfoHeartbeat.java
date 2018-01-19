package com.six.node_manager.discovery;

import com.six.node_manager.NodeInfo;

import lombok.Data;

/**
 * @author sixliu
 * @date 2018年1月19日
 * @email 359852326@qq.com
 * @Description
 */
@Data
public class NodeInfoHeartbeat {

	private NodeInfo nodeInfo;
	private long lastHeartbeatTime;
}
