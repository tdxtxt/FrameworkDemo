package com.tdxtxt.mdbinder.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.View
import com.blankj.utilcode.util.ThreadUtils
import com.tdxtxt.baselib.dialog.impl.CommDialog
import com.tdxtxt.baselib.tools.ToastHelper
import com.tdxtxt.baselib.tools.timer.CountDownTimer
import com.tdxtxt.baselib.tools.timer.TimerListener
import com.tdxtxt.baselib.ui.CommToolBarActivity
import com.tdxtxt.baselib.ui.viewbinding.IViewBinding
import com.tdxtxt.binder.R
import com.tdxtxt.binder.databinding.ActivityMdbinderPClientAidlBinding
import com.tdxtxt.mdbinder.bean.UserInfo
import com.tdxtxt.mdbinder.service.IAidlServiceInterface
import com.tdxtxt.mdbinder.service.AidlRemoteService

/**
 * <pre>
 *     author : tangdexiang
 *     time   : 2023-11-02
 *     desc   :
 * </pre>
 */
class AidlRemoteServiceActivity : CommToolBarActivity(), IViewBinding<ActivityMdbinderPClientAidlBinding> {
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
    override fun getLayoutResId() = R.layout.activity_mdbinder_p_client_aidl
    override fun view2Binding(rootView: View) = ActivityMdbinderPClientAidlBinding.bind(rootView)
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
    }

    private fun excuteRemoteService(task: (() -> Unit)? = null){
        if(mRemoteService != null){
            task?.invoke()
            return
        }

        val intent = Intent(this, AidlRemoteService::class.java)
        bindService(intent, object : ServiceConnection {
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

    inner class CTimerListener(countTime: Int) : TimerListener(countTime, AAClientActivity::class.hashCode()) {
        override fun onTick(second: Int) {
            viewbinding().btnSetClient.text = String.format("绑定binder到服务端，%s秒后再回调过来", if(second == 0) 3 else second)
        }
    }
}