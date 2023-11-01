package com.tdxtxt.mdbinder.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import com.tdxtxt.mdbinder.bean.UserInfo

/**
 * <pre>
 *     author : tangdexiang
 *     time   : 2023-10-30
 *     desc   :
 * </pre>
 */
class MessengerRemoteService : Service() {
    private val mHandler by lazy {
        object : Handler(Looper.getMainLooper()){
            override fun handleMessage(msg: Message) {
                when(msg.what){
                    10 -> {
                        val bundle = msg.data?: return
                        //不设置classLoader将会造成反序列化时报错：ClassNotFoundException when unmarshalling
                        bundle.classLoader = UserInfo::class.java.classLoader
                        val user:UserInfo? = bundle.getParcelable("user")

                        val message = Message.obtain(null, 20)
                        message.data = Bundle().apply { putParcelable("user", user) }
                        msg.replyTo.send(message)
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