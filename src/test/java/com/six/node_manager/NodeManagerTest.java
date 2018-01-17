package com.six.node_manager;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class NodeManagerTest {

	public static void main(String[] args) {
		NodeManagerBuilder builder = new NodeManagerBuilder();
		builder.setClusterEnable(true);
		builder.setClusterName("cluster");
		builder.setNodeName("node1");
		builder.setHost("127.0.0.1");
		builder.setPort(8888);
		NodeManager nodeManager = builder.build();
		nodeManager.getNodeEventManager().registerNodeEventListen(NodeEventType.INIT_BECAOME_MASTER, node -> {

		});
		nodeManager.start();

		nodeManager.stop();
	}

}
