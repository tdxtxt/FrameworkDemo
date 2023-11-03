https://github.com/tdxtxt/FrameworkDemo.git
# 跨进程通信

### 一、使用AIDL通信，支持双向通讯
##### 服务端代码
* 定义aidl文件：提供给客户端调用的接口
```java
package com.tdxtxt.service;

import com.tdxtxt.bean.UserInfo;
import com.tdxtxt.client.IAidlClientInterface;

interface IAidlServiceInterface {
        /*同步调用:根据id获取用户名称，关键字in表示参数只接受服务端读取客户端输入的内容**/
    	String queryUserName(in String userId);
    	/*同步调用:根据id获取用户信息，关键字out表示参数只接受服务端写入客户端输出的内容**/
    	void queryUserInfo(in String userId, out UserInfo userInfo);
        /*异步调用(参数不能用out、input关键字；不能有返回值):设置客户端的Ibinder**/
    	oneway void setClientIBinder(IAidlClientInterface client);
}
```
* 定义aidl文件：传递序列号的对象
```java
package com.tdxtxt.bean;

parcelable UserInfo;
```
* 定义java文件：传递序列号的对象的java实现类，一定要实现readFromParcel方法
```kotlin
package com.tdxtxt.bean

import android.os.Parcel
import android.os.Parcelable

class UserInfo() : Parcelable {
    var id: String? = null
    var name: String? = null

    constructor(id: String?, name: String?): this(){
        this.id = id
        this.name = name
    }
    /*此方法必不可少**/
    fun readFromParcel(parcel: Parcel){
        id = parcel.readString()
        name = parcel.readString()
    }
	...序列化方法的具体实现
}
```
* 继承Service组件，实现aidl内部Stub接口构造IBinder对象，通过onBinder方法并返回
```kotlin
class RemoteService: Service() {
    private var mClicentInterface: IAidlClientInterface? = null
    private val mServiceBinder by lazy {
        object : IAidlServiceInterface.Stub() {
            override fun queryUserName(userId: String): String {
                val serviceUser = DBRepository.queryUserInfo(userId)
                return serviceUser?.name?: "userId不存在"
            }
            override fun queryUserInfo(userId: String, clientUser: UserInfo?) {
                val serviceUser = DBRepository.queryUserInfo(userId)
                clientUser?.id = serviceUser?.id
                clientUser?.name = serviceUser?.name
            }
            override fun setClientIBinder(client: IAidlClientInterface?) {
                mClicentInterface = client
                //linkToDeath监听连接断开的回调
                mClicentInterface?.asBinder()?.linkToDeath({
                     ToastHelper.showToast("客户端已关闭")
                     mClicentInterface = null
                }, 0)
                ThreadUtils.runOnUiThreadDelayed({
                     mClicentInterface?.showUserInfoDialog(DBRepository.queryUserInfo("1004"))
                }, 3000)
            }
        }
    }
    override fun onBind(intent: Intent?): IBinder {
        return mServiceBinder
    }
}
```
*  配置进程（根据情况来）
```
<service android:name="com.tdxtxt.service.RemoteService"
     android:process=":service"
     android:exported="true"/>
```
##### 客户端代码
*  复制服务端定义的aidl文件，主要包括IAidlServiceInterface、UserInfo
* 【双向通讯】定义aidl文件：提供给服务端调用的接口
```java
package com.tdxtxt.client;

import com.tdxtxt.bean.UserInfo;

interface IAidlClientInterface {
    void showUserInfoDialog(in UserInfo userInfo);
}
```
* 启动远程的Service，并进行调用
```java
private var mRemoteService: IAidlServiceInterface? = null
private fun excuteRemoteService(task: (() -> Unit)? = null){
        if(mRemoteService != null){
            task?.invoke()
            return
        }
        val intent = Intent()
        intent.setClassName(this, "com.tdxtxt.service.RemoteService")
        bindService(intent, object : ServiceConnection{
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                mRemoteService = IAidlServiceInterface.Stub.asInterface(service)
                task?.invoke()
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                ToastHelper.showToast("远程服务已关闭")
                mRemoteService = null
            }
        }, Context.BIND_AUTO_CREATE)
}

/*调用服务端接口:queryUserName**/
excuteRemoteService(task){
	val userName = mRemoteService?.queryUserName("1001")
	ToastHelper.showToast(userName)
}

/*调用服务端接口:queryUserInfo**/
excuteRemoteService(){
	val result = UserInfo()
    mRemoteService?.queryUserInfo("1002", result)
    ToastHelper.showToast(result.toString())
}

/*调用服务端接口:setClientIBinder,用于双向通讯**/
private val mClicentBinder by lazy {
     object : IAidlClientInterface.Stub(){
          override fun showUserInfoDialog(userInfo: UserInfo?) {
              ThreadUtils.runOnUiThread {
                  CommDialog.showCommDialog(fragmentActivity, userInfo?.toString())
              }
          }
     }
}
excuteRemoteService(){
	mRemoteService?.setClientIBinder(mClicentBinder)
}
```

### 二、使用Messenger通信，支持双向通讯
##### 服务端代码
* 继承Service组件，实例化Messenger对象，通过onBinder方法并返回Messenger.getBinder()
```kotlin
class MessengerRemoteService : Service() {
    private val mHandler by lazy {
        object : Handler(Looper.getMainLooper()){
            override fun handleMessage(fromClientMessage: Message) {
                when(msg.what){
                    10 -> {
                        val bundle = fromClientMessage.data?: return
                        //不设置classLoader将会造成反序列化时报错：ClassNotFoundException when unmarshalling
                        bundle.classLoader = UserInfo::class.java.classLoader
                        val user:UserInfo? = bundle.getParcelable("user")

                        //给客户端端发消息
                        val toClientMessage = Message.obtain(null, 20)
                        toClientMessage.data = Bundle().apply { putParcelable("user", user) }
                        fromClientMessage.replyTo.send(message)
                    }
                }
            }
        }
    }
    private val mServiceMessenger by lazy { Messenger(mHandler) }
    override fun onBind(intent: Intent?): IBinder? {
        return mServiceMessenger.binder
    }
}
```
*  配置进程（根据情况来）
```
<service
     android:name="com.tdxtxt.service.MessengerRemoteService"
     android:process=":service"
     android:exported="true"/>
```
##### 客户端代码
* 启动远程的Service，并发送消息
```kotlin
private val mHandler by lazy {
     object : Handler(Looper.getMainLooper()){
         override fun handleMessage(fromServiceMessage: Message) {
             when(msg.what){
                 20 -> {
                            val bundle = fromServiceMessage.data?: return
                            //不设置classLoader将会造成反序列化时报错：ClassNotFoundException when unmarshalling
                            bundle.classLoader = UserInfo::class.java.classLoader
                            val user:UserInfo? = bundle.getParcelable("user")

                            CommDialog.showCommDialog(fragmentActivity, String.format("添加成功：%s", user?.toString()))
                    	}
            }
         }
    }
}
private val mClientMessenger by lazy { Messenger(mHandler) }
private var mRemoteServiceMessenger: Messenger? = null
private fun sendMessageRemoteService(message: Message){
     message.replyTo = mClientMessenger
     if(mRemoteServiceMessenger != null){
         mRemoteServiceMessenger?.send(message)
         return
     }
     val intent = Intent()
     intent.setClassName(this, "com.tdxtxt.service.MessengerRemoteService")
     bindService(intent, object : ServiceConnection{
         override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
             mRemoteServiceMessenger = Messenger(service)
             mRemoteServiceMessenger?.send(message)
         }
         override fun onServiceDisconnected(name: ComponentName?) {
             ToastHelper.showToast("远程服务已关闭")
             mRemoteServiceMessenger = null
         }
     }, Context.BIND_AUTO_CREATE)
}

/*发送消息到服务端，注意设置message.replyTo = mClientMessenger用于双向通讯**/
val message = Message.obtain(null, 10)
message.data = Bundle().apply { putParcelable("user", UserInfo("2001", "客户端的User")) }
sendMessageRemoteService(message)

```
