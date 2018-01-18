package com.six.node_manager;

import java.io.Serializable;

import lombok.Data;

/**
 * @author sixliu
 * @date 2017年12月21日
 * @email 359852326@qq.com
 * @Description
 */
@Data
public class NodeInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4806335102450858328L;
	private String clusterName;
	private String name;
	private String host;
	private int port;
	private NodeState state;
	private long version;

	@Override
	public String toString() {
		return "{clusterName=" + clusterName + ", name=" + name + ", host=" + host + ", port=" + port + ", state="
				+ state + ", version=" + version + "}";
	}

	public NodeInfo copy() {
		NodeInfo copy = new NodeInfo();
		copy.clusterName = this.clusterName;
		copy.name = this.name;
		copy.host = this.host;
		copy.port = this.port;
		copy.state = this.state;
		copy.version = this.version;
		return copy;
	}
}
