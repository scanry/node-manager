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

}
