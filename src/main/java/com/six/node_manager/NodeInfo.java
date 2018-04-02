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
	private NodeStatus state;
	private long version;

	@Override
	public int hashCode() {
		int hashCode = 0;
		if (null != clusterName) {
			hashCode += clusterName.hashCode();
		}
		if (null != name) {
			hashCode += name.hashCode();
		}
		if (null != host) {
			hashCode += host.hashCode();
		}
		if (0 != port) {
			hashCode += String.valueOf(port).hashCode();
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object ob) {
		if (null != ob && ob instanceof NodeInfo) {
			NodeInfo target = (NodeInfo) ob;
			return equals(clusterName, target.clusterName) && equals(name, target.name) && equals(host, target.host)
					&& port == target.port;
		}
		return false;
	}

	private static boolean equals(String value1, String value2) {
		if (null != value1 && value1.equals(value2)) {
			return true;
		}
		return false;
	}

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
