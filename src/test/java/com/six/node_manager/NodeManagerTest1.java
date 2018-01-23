package com.six.node_manager;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
public class NodeManagerTest1 {

	public static void main(String[] args) {
		NodeManagerBuilder builder = new NodeManagerBuilder();
		builder.setClusterName("cluster");
		builder.setNodeName("node1");
		builder.setHost("127.0.0.1");
		builder.setPort(8881);
		String discoveryNodes = "node1@127.0.0.1:8881;node2@127.0.0.1:8882;node3@127.0.0.1:8883";
		builder.setDiscoveryNodes(discoveryNodes);
		NodeManager nodeManager = builder.build();
		nodeManager.getNodeEventManager().registerNodeEventListen(NodeEventType.INIT_BECOME_MASTER, node -> {
			System.out.println("master:" + nodeManager.getMasterNode());
			System.out.println("我是master:" + System.currentTimeMillis());
		});
		nodeManager.getNodeEventManager().registerNodeEventListen(NodeEventType.BECOME_SLAVE, node -> {
			System.out.println("master:" + nodeManager.getMasterNode());
			System.out.println("我是slave:" + System.currentTimeMillis());
		});
		nodeManager.getNodeEventManager().registerNodeEventListen(NodeEventType.SLAVE_JOIN, node -> {
			System.out.println("master:" + nodeManager.getMasterNode());
			System.out.println("slave join:" + node);
			System.out.println("slave size:" + nodeManager.getSlaveNods().size());
		});
		nodeManager.getNodeEventManager().registerNodeEventListen(NodeEventType.MISS_MASTER, node -> {
			System.out.println("master:" + nodeManager.getMasterNode());
			System.out.println("miss master:" + node);
		});
		nodeManager.getNodeEventManager().registerNodeEventListen(NodeEventType.MISS_SLAVE, node -> {
			System.out.println("master:" + nodeManager.getMasterNode());
			System.out.println("miss slave:" + node);
		});
		nodeManager.start();
		try {
			Thread.sleep(1111111111);
		} catch (InterruptedException e) {
		}
		nodeManager.stop();
	}

}
