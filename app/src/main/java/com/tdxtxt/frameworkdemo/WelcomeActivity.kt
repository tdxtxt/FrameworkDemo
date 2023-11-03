package com.tdxtxt.frameworkdemo

import android.content.Intent
import android.view.View
import com.tdxtxt.baselib.ui.CommToolBarActivity
import com.tdxtxt.baselib.ui.viewbinding.IViewBinding
import com.tdxtxt.frameworkdemo.databinding.ActivityMdappWelcomeBinding
import com.tdxtxt.mdbinder.client.AAClientActivity

/**
 * <pre>
 *     author : tangdexiang
 *     time   : 2023-11-01
 *     desc   :
 * </pre>
 */
class WelcomeActivity : CommToolBarActivity(), IViewBinding<ActivityMdappWelcomeBinding> {
    override fun getLayoutResId() = R.layout.activity_mdapp_welcome
    override fun view2Binding(rootView: View) = ActivityMdappWelcomeBinding.bind(rootView)
    override fun initUi() {
        clickView(viewbinding().btnBinder)
    }
    private fun clickView(view: View?){
        view?.setOnClickListener {
            when(it.id){
                R.id.btn_binder -> {
                    startActivity(Intent(this, AAClientActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                }
            }
        }
    }
}