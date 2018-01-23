package com.six.node_manager;

import java.util.Comparator;

/**
 * @author sixliu
 * @date 2018年1月22日
 * @email 359852326@qq.com
 * @Description
 */
public interface NodeResourceCollect{

	NodeResource collect(String nodeName);
	
	Comparator<NodeResource> getComparator();
}
