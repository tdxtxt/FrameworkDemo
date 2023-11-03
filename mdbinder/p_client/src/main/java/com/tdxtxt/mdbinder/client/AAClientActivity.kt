package com.tdxtxt.mdbinder.client

import android.content.Intent
import android.view.View
import com.tdxtxt.baselib.ui.CommToolBarActivity
import com.tdxtxt.baselib.ui.viewbinding.IViewBinding
import com.tdxtxt.binder.R
import com.tdxtxt.binder.databinding.ActivityMdbinderPClientMainBinding

/**
 * <pre>
 *     author : tangdexiang
 *     time   : 2023-10-30
 *     desc   :
 * </pre>
 */
class AAClientActivity : CommToolBarActivity(), IViewBinding<ActivityMdbinderPClientMainBinding>{
    override fun getLayoutResId() = R.layout.activity_mdbinder_p_client_main
    override fun view2Binding(rootView: View) = ActivityMdbinderPClientMainBinding.bind(rootView)
    override fun initUi() {
        setTitleBar("跨进程通讯")
        clickView(viewbinding().btnAidl)
        clickView(viewbinding().btnMessenger)
        clickView(viewbinding().btnSocket)
    }
    private fun clickView(view: View?){
        view?.setOnClickListener {
            when(it.id){
                R.id.btn_aidl -> startActivity(Intent(this, AidlRemoteServiceActivity::class.java))
                R.id.btn_messenger -> startActivity(Intent(this, MessengerRemoteServiceActivity::class.java))
                R.id.btn_socket -> startActivity(Intent(this, SocketRemoteServiceActivity::class.java))
            }
        }
    }

}