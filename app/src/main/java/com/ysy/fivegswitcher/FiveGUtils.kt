package com.ysy.fivegswitcher

import android.util.Log

object FiveGUtils {

    private const val TAG = "FiveGUtils"

    fun isFiveGCapable(): Boolean =
        try {
            ReflectUtils.reflect("miui.telephony.TelephonyManager")
                .method("getDefault")
                .method("isFiveGCapable")
                .get()
        } catch (e: Exception) {
            Log.e(TAG, "isFiveGCapable", e)
            false
        }

    fun setUserFiveGEnabled(enable: Boolean) {
        try {
            ReflectUtils.reflect("miui.telephony.TelephonyManager")
                .method("getDefault")
                .method("setUserFiveGEnabled", enable)
        } catch (e: Exception) {
            Log.e(TAG, "setUserFiveGEnabled $enable", e)
        }
    }

    fun isUserFiveGEnabled(): Boolean =
        try {
            ReflectUtils.reflect("miui.telephony.TelephonyManager")
                .method("getDefault")
                .method("isUserFiveGEnabled")
                .get()
        } catch (e: Exception) {
            Log.e(TAG, "isUserFiveGEnabled", e)
            false
        }
}
