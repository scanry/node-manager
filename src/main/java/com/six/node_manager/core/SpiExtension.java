package com.six.node_manager.core;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.service.Service;

/**
*@author:MG01867
*@date:2018年1月26日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public class SpiExtension {
	
	private static Logger log = LoggerFactory.getLogger(SpiExtension.class);
	
	static class SpiExtensionInner{
		private static SpiExtension instance=new SpiExtension();
	}

	private Map<Class<?>,Object> instanceMap=new LinkedHashMap<>();
	
	private SpiExtension() {}
	
	public static SpiExtension getInstance() {
		return SpiExtensionInner.instance;
	}
	
	public <T>T find(Class<T> clz){
		Objects.requireNonNull(clz);
		Object instance=instanceMap.get(clz);
		return clz.cast(instance);
	}
	
	public <T>void register(Class<T> clz,T instance){
		Objects.requireNonNull(clz);
		Objects.requireNonNull(instance);
		if (!clz.isAssignableFrom(instance.getClass())&&!clz.equals(instance.getClass())) {
			throw new IllegalArgumentException();
		}
		instanceMap.put(clz, instance);
	}
		
	public Collection<Object> allInstance() {
		return instanceMap.values();
	}
	
	public void startAll() {
		for(Object ob:allInstance()) {
			if(ob instanceof Service) {
				((Service) ob).start();
			}
		}
	}
	public void stopAll() {
		for(Object ob:allInstance()) {
			if(ob instanceof Service) {
				try {
					((Service) ob).stop();
				}catch (Exception e) {
					log.error("stop service["+((Service) ob).getName()+"] exception", e);
				}
			}
		}
	}
}
