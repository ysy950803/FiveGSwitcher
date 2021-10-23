package com.ysy.fivegswitcher

import android.util.Base64
import android.util.Log

internal object FiveGUtils {

    private const val TAG = "FiveGUtils"
    private val className by lazy { R.string.fgu_class.convertRuntimeName() }
    private val method1 by lazy { R.string.fgu_method_1.convertRuntimeName() }
    private val method2 by lazy { R.string.fgu_method_2.convertRuntimeName() }
    private val method3 by lazy { R.string.fgu_method_3.convertRuntimeName() }
    private val method4 by lazy { R.string.fgu_method_4.convertRuntimeName() }

    fun isFiveGCapable(): Boolean =
        try {
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

    fun isUserFiveGEnabled(): Boolean =
        try {
            ReflectUtils.reflect(className)
                .method(method1)
                .method(method4)
                .get()
        } catch (e: Exception) {
            Log.e(TAG, method4, e)
            false
        }

    private fun Int.convertRuntimeName() =
        String(Base64.decode(FSApp.getContext().getString(this), Base64.DEFAULT))
}
