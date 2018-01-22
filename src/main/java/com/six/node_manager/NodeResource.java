package com.six.node_manager;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author sixliu
 * @date 2018年1月22日
 * @email 359852326@qq.com
 * @Description
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class NodeResource implements Serializable,Comparable<NodeResource>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6088576010589740300L;

	private String nodeName;
	private float cpuUseRate;
	private float memoryUSeRate;
	private float networkUseRate;
	private long lastHeartbeatTime;
}
