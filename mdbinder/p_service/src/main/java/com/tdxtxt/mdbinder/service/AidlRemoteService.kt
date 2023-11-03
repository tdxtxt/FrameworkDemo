package com.tdxtxt.mdbinder.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.blankj.utilcode.util.ThreadUtils
import com.tdxtxt.baselib.tools.ToastHelper
import com.tdxtxt.mdbinder.bean.UserInfo
import com.tdxtxt.mdbinder.client.IAidlClientInterface
import com.tdxtxt.mdbinder.repository.DBRepository

/**
 * <pre>
 *     author : tangdexiang
 *     time   : 2023-10-27
 *     desc   :
 * </pre>
 */
class AidlRemoteService: Service() {
    private var mClicentInterface: IAidlClientInterface? = null
    private val mServiceBinder by lazy {
        object : IAidlServiceInterface.Stub() {
            override fun queryUserName(userId: String): String {
                val serviceUser = DBRepository.queryUserInfo(userId)
                return serviceUser?.name?: "userId不存在"
            }
            override fun queryUserInfo(userId: String, userInfo: UserInfo?) {
                val serviceUser = DBRepository.queryUserInfo(userId)
                userInfo?.id = serviceUser?.id
                userInfo?.name = serviceUser?.name
            }
            override fun setClientIBinder(client: IAidlClientInterface?) {
                mClicentInterface = client
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