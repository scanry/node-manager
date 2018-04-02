package com.six.node_manager.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.six.node_manager.NodeEventType;
import com.six.node_manager.service.AbstractService;
import com.six.node_manager.service.Service;
import com.six.node_manager.NodeEvent;
import com.six.node_manager.NodeEventListen;

/**
 * @author sixliu
 * @date 2018年1月17日
 * @email 359852326@qq.com
 * @Description
 */
public class NodeEventManagerImpl extends AbstractService implements NodeEventManager,Service {

	private static final Logger log = LoggerFactory.getLogger(NodeEventManagerImpl.class);

	private static final NodeEvent END_NODE_EVENT = new NodeEvent(null, null);
	private static final int DEFAULT_MAX_EVENT_QUEUE_SIZE = 10000;
	private int maxEventQueueSize;
	private Thread processEventQueueThread;
	private Map<NodeEventType, Set<NodeEventListen>> nodeEventListens;
	private LinkedBlockingQueue<NodeEvent> eventQueue;

	public NodeEventManagerImpl() {
		this(DEFAULT_MAX_EVENT_QUEUE_SIZE);
	}

	public NodeEventManagerImpl(int maxEventQueueSize) {
		super("NodeEventManager");
		this.maxEventQueueSize = maxEventQueueSize;
		processEventQueueThread = new Thread(() -> {
			processEventQueue();
		}, "NodeEventManager-processEventQueue-Thread");
		nodeEventListens = new ConcurrentHashMap<>();
		eventQueue = new LinkedBlockingQueue<NodeEvent>();
	}

	@Override
	public boolean addNodeEvent(NodeEvent nodeEvent) {
		if (this.eventQueue.size() <= maxEventQueueSize) {
			return this.eventQueue.add(nodeEvent);
		}
		return false;
	}

	@Override
	public void registerNodeEventListen(NodeEventType event, NodeEventListen nodeListen) {
		Objects.requireNonNull(event);
		Objects.requireNonNull(nodeListen);
		nodeEventListens.computeIfAbsent(event, newKey -> new HashSet<>()).add(nodeListen);
	}

	@Override
	protected void doStart() {
		processEventQueueThread.start();
	}

	@Override
	protected void doStop() {
		eventQueue.add(END_NODE_EVENT);
	}

	private void processEventQueue() {
		NodeEvent event = null;
		Set<NodeEventListen> nodeEventListenSet = null;
		log.info("start " + getName() + " service's process Event Queue's thread");
		while (isRunning()) {
			try {
				event = this.eventQueue.take();
				if (END_NODE_EVENT != event && null != (nodeEventListenSet = nodeEventListens.get(event.getType()))) {
					for (NodeEventListen nodeEventListen : nodeEventListenSet) {
						nodeEventListen.listen(event);
					}
				}
			} catch (Exception e) {
				log.warn(getName() + " service has exception. ", e);
			}
		}
		log.info("end " + getName() + " service's process Event Queue's thread");
	}

}
