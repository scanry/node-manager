package com.six.node_manager.noderole;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2018年1月19日 下午7:38:27 类说明
 *          <p>
 *          1.向主节点汇报当前节点心态信息。
 *          </P>
 *          <p>
 *          2.接收来自主节点的写请求。
 *          </P>
 *          <p>
 *          3.实时与主节点进行数据同步
 *          </P>
 */
public interface SlaveNodeRole {

	void heartbeatToMaster();
}
