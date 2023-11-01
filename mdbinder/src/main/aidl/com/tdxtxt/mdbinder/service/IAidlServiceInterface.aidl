package com.tdxtxt.mdbinder.service;

import com.tdxtxt.mdbinder.bean.UserInfo;
import com.tdxtxt.mdbinder.client.IAidlClientInterface;

interface IAidlServiceInterface {
        /*同步调用:根据id获取用户名称**/
    	String queryUserName(in String userId);
    	/*同步调用:根据id获取用户信息**/
    	void queryUserInfo(in String userId, out UserInfo userInfo);
        /*异步调用(参数不能用out、input关键字；不能有返回值):设置客户端的Ibinder**/
    	oneway void setClientIBinder(IAidlClientInterface client);
}