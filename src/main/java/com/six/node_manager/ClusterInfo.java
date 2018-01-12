package com.six.node_manager;

import java.io.Serializable;

import lombok.Data;

/**
 * @author sixliu
 * @date 2018年1月12日
 * @email 359852326@qq.com
 * @Description
 */
@Data
public class ClusterInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5174260932906408683L;
	
	private String clusterName;
	private String masterName;
	private int activitySzie;
	private int deadNodeSize;
}
