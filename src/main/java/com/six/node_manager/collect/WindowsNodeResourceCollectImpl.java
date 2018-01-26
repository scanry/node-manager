package com.six.node_manager.collect;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Comparator;

import com.six.node_manager.NodeResource;
import com.six.node_manager.NodeResourceCollect;

/**
 * @author sixliu
 * @date 2018年1月22日
 * @email 359852326@qq.com
 * @Description
 */
public class WindowsNodeResourceCollectImpl implements NodeResourceCollect {

	private static MemoryMXBean memoryMXBean;
	static {
		memoryMXBean = ManagementFactory.getMemoryMXBean();
	}

	@Override
	public NodeResource collect(String nodeName) {
		NodeResource nodeResource = new NodeResource();
		nodeResource.setNodeName(nodeName);
		MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
		long memoryMax = heapMemoryUsage.getMax();
		long memoryUsed = heapMemoryUsage.getUsed();
		nodeResource.setMemoryUSeRate(memoryUsed / memoryMax);
		return nodeResource;
	}

	@Override
	public Comparator<NodeResource> getComparator() {
		// TODO Auto-generated method stub
		return null;
	}

}
