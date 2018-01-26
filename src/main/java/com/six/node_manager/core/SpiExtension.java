package com.six.node_manager.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.service.Service;

/**
 * @author:MG01867
 * @date:2018年1月26日
 * @E-mail:359852326@qq.com
 * @version:
 * @describe spi扩展接口管理
 */
public class SpiExtension {

	private static Logger log = LoggerFactory.getLogger(SpiExtension.class);

	static class SpiExtensionInner {
		private static SpiExtension instance = new SpiExtension();
	}

	/** 必須是LinkedHashMap 保證先註冊的先启动 **/
	private LinkedHashMap<Class<?>, Object> instanceMap = new LinkedHashMap<>();

	private SpiExtension() {
	}

	public static SpiExtension getInstance() {
		return SpiExtensionInner.instance;
	}

	public <T> T find(Class<T> clz) {
		Objects.requireNonNull(clz);
		Object instance = instanceMap.get(clz);
		return clz.cast(instance);
	}

	public <T> void register(Class<T> clz, T instance) {
		Objects.requireNonNull(clz);
		Objects.requireNonNull(instance);
		if (!clz.isAssignableFrom(instance.getClass()) && !clz.equals(instance.getClass())) {
			throw new IllegalArgumentException();
		}
		instanceMap.put(clz, instance);
	}

	public Collection<Object> allInstance() {
		return instanceMap.values();
	}

	public void startAll() {
		for (Object ob : allInstance()) {
			if (ob instanceof Service) {
				((Service) ob).start();
			}
		}
	}

	public void stopAll() {
		List<Service> instanceList = new ArrayList<>();
		for (Object ob : allInstance()) {
			if (ob instanceof Service) {
				instanceList.add((Service) ob);
			}
		}
		Service service = null;
		for (int i = instanceList.size(); i >= 0; i--) {
			service = instanceList.get(i);
			try {
				service.stop();
			} catch (Exception e) {
				log.error("stop service[" + service.getName() + "] exception", e);
			}
		}
		instanceMap.clear();
	}
}
