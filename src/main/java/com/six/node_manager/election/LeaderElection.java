package com.six.node_manager.election;

import com.six.node_manager.NodeInfo;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public interface LeaderElection {

	NodeInfo election(NodeInfo localNodeInfo);

	void close();
}
