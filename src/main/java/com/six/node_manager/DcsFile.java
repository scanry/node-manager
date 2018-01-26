package com.six.node_manager;
/**
*@author:MG01867
*@date:2018年1月25日
*@E-mail:359852326@qq.com
*@version:
*@describe //TODO
*/
public interface DcsFile {

	void write(int pos,int lenght,byte[] dts);
	
	void read(int pos,int lenght,byte[] dts);
}
