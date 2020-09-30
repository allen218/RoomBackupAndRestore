package com.allen218.roombackupandrestore

import android.util.Log
import autodispose2.FlowableSubscribeProxy
import io.reactivex.rxjava3.disposables.Disposable

/**

 * created：2020/9/30 on 11:50
 */
fun <T> FlowableSubscribeProxy<T>.subscribeBy(
    onNext: ((T) -> Unit)? = null,
    onComplete: (() -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null
): Disposable {
    return this.subscribe(
        { onNext?.invoke(it) },
        {
            Log.e("tag", it.msg)
            onError?.invoke(it)
        },
        { onComplete?.invoke() }
    )
}