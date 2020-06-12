package com.ysy.switcherfiveg

import android.util.Base64
import android.util.Log

internal object FiveGUtils {

    private const val TAG = "FiveGUtils"
    private val consts by lazy { getConsts().split(";") }
    private val method1 by lazy { consts[1].convertRuntimeName() }
    private val method3 by lazy { consts[3].convertRuntimeName() }
    private val method2 by lazy { consts[2].convertRuntimeName() }
    private val method4 by lazy { consts[4].convertRuntimeName() }
    private val className by lazy { consts[0].convertRuntimeName() }

    fun isFiveGCapable(): Boolean = try {
        ReflectUtils.reflect(className)
            .method(method1)
            .method(method2)
            .get()
    } catch (e: Exception) {
        Log.e(TAG, method2, e)
        false
    }

    fun setUserFiveGEnabled(enable: Boolean) {
        try {
            ReflectUtils.reflect(className)
                .method(method1)
                .method(method3, enable)
        } catch (e: Exception) {
            Log.e(TAG, "$method3 $enable", e)
        }
    }

    fun isUserFiveGEnabled(): Boolean = try {
        ReflectUtils.reflect(className)
            .method(method1)
            .method(method4)
            .get()
    } catch (e: Exception) {
        Log.e(TAG, method4, e)
        false
    }

    fun String.convertRuntimeName() = String(Base64.decode(this, Base64.DEFAULT))

    init {
        System.loadLibrary("fivegswitcher")
    }

    private external fun getConsts(): String
}
