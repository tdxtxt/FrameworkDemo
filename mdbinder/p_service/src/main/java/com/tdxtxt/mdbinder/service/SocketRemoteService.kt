package com.tdxtxt.mdbinder.service

import android.app.IntentService
import android.content.Intent
import android.net.LocalServerSocket
import com.tdxtxt.baselib.tools.ToastHelper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.io.StringWriter

/**
 * <pre>
 *     author : tangdexiang
 *     time   : 2023-11-01
 *     desc   :
 * </pre>
 */
class SocketRemoteService : IntentService("SocketRemoteService") {
    private val mSocket by lazy { LocalServerSocket(this::class.java.simpleName) }
    override fun onHandleIntent(intent: Intent?) {
        //子线程执行
        val socket = mSocket.accept() //accept是个阻塞方法，这就是必须要在子线程接收消息的原因。
        while (true){
            try{
                val bufferReader = BufferedReader(InputStreamReader(socket.inputStream))
                val line = bufferReader.readLine()
                if(line != null){
                    ToastHelper.showToast(line)
                    val writer = PrintWriter(socket.outputStream)
                    writer.print(String.format("服务端收到内容：%s", line))
                    writer.flush()
                }
            }catch (ex: Exception){
                ex.printStackTrace()
            }finally {

            }
        }
    }


}