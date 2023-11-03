package com.tdxtxt.mdbinder.client

import android.content.Intent
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.view.View
import com.tdxtxt.baselib.tools.ToastHelper
import com.tdxtxt.baselib.ui.CommToolBarActivity
import com.tdxtxt.baselib.ui.viewbinding.IViewBinding
import com.tdxtxt.binder.R
import com.tdxtxt.binder.databinding.ActivityMdbinderPClientSocketBinding
import com.tdxtxt.mdbinder.service.SocketRemoteService
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter

/**
 * <pre>
 *     author : tangdexiang
 *     time   : 2023-11-02
 *     desc   :
 * </pre>
 */
class SocketRemoteServiceActivity : CommToolBarActivity(), IViewBinding<ActivityMdbinderPClientSocketBinding> {
    private val mClientSokect by lazy { LocalSocket() }
    private val mRemoteSocketAddress by lazy {  LocalSocketAddress(SocketRemoteService::class.java.simpleName) }
    private val mPrintWriter by lazy { PrintWriter(mClientSokect.outputStream) }
    private val mBufferedReader by lazy { BufferedReader(InputStreamReader(mClientSokect.inputStream)) }
    override fun getLayoutResId() = R.layout.activity_mdbinder_p_client_socket
    override fun view2Binding(rootView: View) = ActivityMdbinderPClientSocketBinding.bind(rootView)
    override fun initUi() {
        val intent = Intent(this, SocketRemoteService::class.java)
        startService(intent)
        viewbinding().btnSend.postDelayed({
            connectRemoteSocket()
        }, 300)
        viewbinding().btnSend.setOnClickListener {
            val content = viewbinding().etInput.text.toString()
            sendMessage(content)
        }
    }
    private fun connectRemoteSocket(){
        mClientSokect.connect(mRemoteSocketAddress) //连接TestLocalSocketServer
    }
    private fun sendMessage(message: String?){
        mPrintWriter.print(message)
//        val line = mBufferedReader.readLine()
//        ToastHelper.showToast(line)
    }

}