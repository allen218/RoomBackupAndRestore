package com.allen218.roombackupandrestore

import android.content.Intent
import android.util.Log
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before


/**
 * author:allen
 * created：2020/9/29 on 16:07
 */
@Aspect
class InterceptDbChanged {
    val TAG = InterceptDbChanged::class.java.simpleName

    @Before("execution(* executeInsert(..))")
    @Throws(Throwable::class)
    fun insertMethod(joinPoint: JoinPoint) {
        nodifyChanged()
        Log.e(TAG, "nodifyChanged()")
    }

    private fun nodifyChanged() {
        appCtx.startService(Intent(appCtx, DbBackupAndRestoreService::class.java))
    }

    @Before("execution(* executeUpdateDelete(..))")
    @Throws(Throwable::class)
    fun updateMethod(joinPoint: JoinPoint) {
        nodifyChanged()
        Log.e(TAG, "nodifyChanged()")
    }
}