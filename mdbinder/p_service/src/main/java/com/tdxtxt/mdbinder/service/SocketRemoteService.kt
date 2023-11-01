package com.tdxtxt.mdbinder.service

import android.app.Service
import android.content.Intent
import android.net.LocalServerSocket
import android.os.IBinder

/**
 * <pre>
 *     author : tangdexiang
 *     time   : 2023-11-01
 *     desc   :
 * </pre>
 */
class SocketRemoteService : Service() {
    private val mSocket by lazy { LocalServerSocket("com.tdxtxt.mdbinder.service.SocketRemoteService") }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


}