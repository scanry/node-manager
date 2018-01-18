package com.six.node_manager.discovery.rpc;

import com.six.node_manager.NodeInfo;

import lombok.Data;

/**
 * @author sixliu
 * @date 2018年1月18日
 * @email 359852326@qq.com
 * @Description
 */
@Data
public class MasterProposal {
	private int logicClock;
	private String proposer;
	private NodeInfo master;

	public String toString() {
		return "[proposer:" + proposer + ",logicClock:" + logicClock + ",master:" + master.getName() + ",version:"
				+ master.getVersion() + "]";
	}
}
