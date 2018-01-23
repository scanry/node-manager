package com.six.node_manager.core;


import java.util.Comparator;

import com.six.node_manager.NodeResource;
import com.six.node_manager.NodeResourceCollect;

/**   
 * @author sixliu   
 * @date   2018年1月23日 
 * @email  359852326@qq.com  
 * @Description 
 */
public class LinuxNodeResourceCollectImpl implements NodeResourceCollect {

	@Override
	public NodeResource collect(String nodeName) {
		NodeResource nodeResource = new NodeResource();
		nodeResource.setNodeName(nodeName);
		return nodeResource;
	}

	@Override
	public Comparator<NodeResource> getComparator() {
		// TODO Auto-generated method stub
		return null;
	}

}

