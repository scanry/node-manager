package com.six.node_manager;


/**   
 * @author sixliu   
 * @date   2018年1月18日 
 * @email  359852326@qq.com  
 * @Description 
 */
public class NodeManagerTest3 {

	public static void main(String[] args) {
		NodeManagerLauncher builder = new NodeManagerLauncher();
		builder.setClusterName("cluster");
		builder.setNodeName("node3");
		builder.setHost("127.0.0.1");
		builder.setPort(8883);
		String discoveryNodes = "node1@127.0.0.1:8881;node2@127.0.0.1:8882;node3@127.0.0.1:8883";
		builder.setDiscoveryNodes(discoveryNodes);
		NodeDiscovery nodeDiscovery = builder.build();
		nodeDiscovery.registerNodeEventListener(NodeEventType.INIT_BECOME_MASTER, node -> {
			System.out.println("master:" + nodeDiscovery.getMasterNodeInfo());
			System.out.println("我是master:" + System.currentTimeMillis());
		});
		nodeDiscovery.registerNodeEventListener(NodeEventType.BECOME_SLAVE, node -> {
			System.out.println("master:" +nodeDiscovery.getMasterNodeInfo());
			System.out.println("我是slave:" + System.currentTimeMillis());
		});
		nodeDiscovery.registerNodeEventListener(NodeEventType.SLAVE_JOIN, node -> {
			System.out.println("master:" + nodeDiscovery.getMasterNodeInfo());
			System.out.println("slave join:" + node);
			System.out.println("slave size:" + nodeDiscovery.getAllSlaveNodeInfos().size());
		});
		nodeDiscovery.registerNodeEventListener(NodeEventType.MISS_MASTER, node -> {
			System.out.println("master:" + nodeDiscovery.getMasterNodeInfo());
			System.out.println("miss master:" + node);
		});
		nodeDiscovery.registerNodeEventListener(NodeEventType.MISS_SLAVE, node -> {
			System.out.println("master:" + nodeDiscovery.getMasterNodeInfo());
			System.out.println("miss slave:" + node);
		});
		nodeDiscovery.start();
		try {
			Thread.sleep(1111111111);
		} catch (InterruptedException e) {
		}
		nodeDiscovery.stop();
	}

}

