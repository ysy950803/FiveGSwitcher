package com.ysy.fivegswitcher

object FiveGUtils {

    fun isFiveGCapable(): Boolean {
        return try {
            ReflectUtils.reflect("miui.telephony.TelephonyManager")
                .method("getDefault")
                .method("isFiveGCapable")
                .get()
        } catch (e: Exception) {
            false
        }
    }

    fun setUserFiveGEnabled(enable: Boolean) {
        try {
            ReflectUtils.reflect("miui.telephony.TelephonyManager")
                .method("getDefault")
                .method("setUserFiveGEnabled", enable)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun isUserFiveGEnabled(): Boolean {
        return try {
            ReflectUtils.reflect("miui.telephony.TelephonyManager")
                .method("getDefault")
                .method("isUserFiveGEnabled")
                .get()
        } catch (e: Exception) {
            false
        }
    }
}
