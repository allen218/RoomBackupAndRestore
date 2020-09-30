package com.allen218.roombackupandrestore

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**

 * created：2020/9/29 on 20:14
 */
class BroadcastRegister : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("TAG", "OnReceiver")
    }
}