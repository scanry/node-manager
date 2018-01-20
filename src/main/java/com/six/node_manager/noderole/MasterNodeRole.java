package com.six.node_manager.noderole;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2018年1月19日 下午7:37:53 类说明
 *          <p>
 * 			1.检查所有加入集群的从节点的心态更新。
 *          </P>
 *          <p>
 * 			2.向所有从节点发布写操作。
 *          </P>
 *          <p>
 *          3.向所有从节点提供数据同步请求。
 *          </P>
 */
public interface MasterNodeRole {

	void checkSlaveHeartbeat();
}
