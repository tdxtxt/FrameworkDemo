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
import com.blankj.utilcode.util.ThreadUtils
import com.tdxtxt.baselib.dialog.impl.CommDialog
import com.tdxtxt.baselib.tools.ToastHelper
import com.tdxtxt.baselib.tools.timer.CountDownTimer
import com.tdxtxt.baselib.tools.timer.TimerListener
import com.tdxtxt.baselib.ui.CommToolBarActivity
import com.tdxtxt.baselib.ui.viewbinding.IViewBinding
import com.tdxtxt.binder.R
import com.tdxtxt.binder.databinding.ActivityMdbinderPClientManangerBinding
import com.tdxtxt.mdbinder.bean.UserInfo
import com.tdxtxt.mdbinder.service.IAidlServiceInterface
import com.tdxtxt.mdbinder.service.MessengerRemoteService
import com.tdxtxt.mdbinder.service.RemoteService

/**
 * <pre>
 *     author : tangdexiang
 *     time   : 2023-10-30
 *     desc   :
 * </pre>
 */
class ClientManangerActivity : CommToolBarActivity(), IViewBinding<ActivityMdbinderPClientManangerBinding> {
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
    private var mRemoteService: IAidlServiceInterface? = null
    private val mClicentBinder by lazy {
        object : IAidlClientInterface.Stub(){
            override fun showUserInfoDialog(userInfo: UserInfo?) {
                ThreadUtils.runOnUiThread {
                    CommDialog.showCommDialog(fragmentActivity, userInfo?.toString())
                }
            }
        }
    }
    private val mTimerListener by lazy { CTimerListener(3) }
    override fun getLayoutResId() = R.layout.activity_mdbinder_p_client_mananger
    override fun view2Binding(rootView: View) = ActivityMdbinderPClientManangerBinding.bind(rootView)
    override fun initUi() {
        viewbinding().btnFindName.setOnClickListener {
            val task = {
                val userName = mRemoteService?.queryUserName("1001")
                ToastHelper.showToast(userName)
            }
            excuteRemoteService(task)
        }
        viewbinding().btnFindUser.setOnClickListener {
            val task = {
                val result = UserInfo()
                mRemoteService?.queryUserInfo("1002", result)
                ToastHelper.showToast(result.toString())
            }
            excuteRemoteService(task)
        }
        viewbinding().btnSetClient.setOnClickListener {
            val task = {
                mRemoteService?.setClientIBinder(mClicentBinder)
                mTimerListener.setCountDownSecond(3)
                CountDownTimer.getInstance().addTimerListener(mTimerListener)
            }
            excuteRemoteService(task)
        }
        viewbinding().btnAddUser.setOnClickListener {
            val message = Message.obtain(null, 10)
            message.data = Bundle().apply { putParcelable("user", UserInfo("2001", "客户端的User")) }
            sendMessageRemoteService(message)
        }
    }

    private fun excuteRemoteService(task: (() -> Unit)? = null){
        if(mRemoteService != null){
            task?.invoke()
            return
        }

        val intent = Intent(this, RemoteService::class.java)
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
    private fun sendMessageRemoteService(message: Message){
        message.replyTo = mClientMessenger
        if(mRemoteServiceMessenger != null){
            mRemoteServiceMessenger?.send(message)
            return
        }
        val intent = Intent(this, MessengerRemoteService::class.java)
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

    inner class CTimerListener(countTime: Int) : TimerListener(countTime, ClientManangerActivity::class.hashCode()) {
        override fun onTick(second: Int) {
            viewbinding().btnSetClient.text = String.format("绑定binder到服务端，%s秒后再回调过来", if(second == 0) 3 else second)
        }

    }
}