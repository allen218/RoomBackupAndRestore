package com.allen218.roombackupandrestore

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.allen218.roombackupandrestore.db.DbManager
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

/**

 * created：2020/9/30 on 11:47
 */
class DbBackupAndRestoreService : Service() {
    var needUpdated = false
    private val composeDisposable = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        startBackupTask()
    }

    private fun startBackupTask() {
        val disposable = Flowable.interval(0, 5 * 1000, TimeUnit.MILLISECONDS)
            .filter { needUpdated }
            .doOnNext { backup() }
            .doOnNext { needUpdated = false }
            .subscribe({}, { t: Throwable ->
                Log.e("TAG", t.msg)
            })
        composeDisposable.add(disposable)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        needUpdated = true
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        composeDisposable.clear()
    }

    private fun backup() {
        DbManager.backup()
    }

}