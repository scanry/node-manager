package com.six.node_manager.core;

import java.lang.management.ManagementFactory;

import com.six.node_manager.NodeResourceCollect;

/**   
 * @author sixliu   
 * @date   2018年1月23日 
 * @email  359852326@qq.com  
 * @Description 
 */
public class NodeResourceCollectFactory {
	static enum OsType{
		WINDOWS,
		LINUX,
		UNIX,
		MAC,
		UNS_SUPPORT;
	}
	private static OsType OS_TYPE;
	static {
		String osName=ManagementFactory.getOperatingSystemMXBean().getName();
		if(osName.toUpperCase().contains("WINDOWS")) {
			OS_TYPE=OsType.WINDOWS;
		}else if(osName.toUpperCase().contains("LINUX")) {
			OS_TYPE=OsType.LINUX;
		}else{
			OS_TYPE=OsType.UNS_SUPPORT;
		}
	}
	
	public static NodeResourceCollect newNodeResourceCollect() {
		NodeResourceCollect nodeResourceCollect=null;
		if(OsType.WINDOWS==OS_TYPE) {
			nodeResourceCollect=new WindowsNodeResourceCollectImpl();
		}else if(OsType.WINDOWS==OS_TYPE) {
			nodeResourceCollect=new LinuxNodeResourceCollectImpl();
		}else {
			throw new UnsupportedOperationException();
		}
		return nodeResourceCollect;
	}
	
	public static void main(String[] a) {
		NodeResourceCollect nodeResourceCollect=newNodeResourceCollect();
		System.out.println(nodeResourceCollect.getClass());
	}
}

