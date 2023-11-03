package com.tdxtxt.mdbinder.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.view.View
import com.tdxtxt.baselib.dialog.impl.CommDialog
import com.tdxtxt.baselib.tools.ToastHelper
import com.tdxtxt.baselib.ui.CommToolBarActivity
import com.tdxtxt.baselib.ui.viewbinding.IViewBinding
import com.tdxtxt.binder.R
import com.tdxtxt.binder.databinding.ActivityMdbinderPClientMessengerBinding
import com.tdxtxt.mdbinder.bean.UserInfo
import com.tdxtxt.mdbinder.service.MessengerRemoteService

/**
 * <pre>
 *     author : tangdexiang
 *     time   : 2023-11-02
 *     desc   :
 * </pre>
 */
class MessengerRemoteServiceActivity : CommToolBarActivity(), IViewBinding<ActivityMdbinderPClientMessengerBinding> {
    private val mHandler by lazy {
        object : Handler(Looper.getMainLooper()){
            override fun handleMessage(msg: Message) {
                when(msg.what){
                    20 -> {
                        val bundle = msg.data?: return
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
    override fun getLayoutResId() = R.layout.activity_mdbinder_p_client_messenger
    override fun view2Binding(rootView: View) = ActivityMdbinderPClientMessengerBinding.bind(rootView)
    override fun initUi() {
        viewbinding().btnAddUser.setOnClickListener {
            val message = Message.obtain(null, 10)
            message.data = Bundle().apply { putParcelable("user", UserInfo("2001", "客户端的User")) }
            sendMessageRemoteService(message)
        }
    }

    private fun sendMessageRemoteService(message: Message){
        message.replyTo = mClientMessenger
        if(mRemoteServiceMessenger != null){
            mRemoteServiceMessenger?.send(message)
            return
        }
        val intent = Intent(this, MessengerRemoteService::class.java)
        bindService(intent, object : ServiceConnection {
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
}