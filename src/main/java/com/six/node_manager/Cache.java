package com.six.node_manager;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2018年1月17日 下午8:04:21 类说明
 */
public interface Cache{

	void put(String key, String value);

	String get(String key);
	
	String remove(String key);
}
