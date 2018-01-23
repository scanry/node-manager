package com.six.node_manager.discovery;

import java.io.Serializable;

import com.six.node_manager.NodeInfo;

import lombok.Data;

/**
 * @author sixliu
 * @date 2018年1月18日
 * @email 359852326@qq.com
 * @Description
 */
@Data
public class MasterProposal implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 112963061081381636L;
	private int logicClock;
	private String proposer;
	private NodeInfo node;

	public String toString() {
		return "[proposer:" + proposer + ",logicClock:" + logicClock + ",master:" + node.getName() + ",version:"
				+ node.getVersion() + "]";
	}
}
