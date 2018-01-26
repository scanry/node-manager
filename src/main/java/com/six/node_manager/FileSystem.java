package com.six.node_manager;
/**
* @author sixliu E-mail:359852326@qq.com
* @version 创建时间：2018年1月17日 下午8:31:32
* 类说明
*/
public interface FileSystem {

	DcsFile find(String path);
	
	void create(DcsFile file);
	
	int delete(String path);
}
