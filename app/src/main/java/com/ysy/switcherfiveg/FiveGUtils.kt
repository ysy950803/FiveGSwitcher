package com.ysy.switcherfiveg

import android.provider.Settings
import android.util.Base64
import android.util.Log
import androidx.preference.PreferenceManager
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.ReflectUtils
import com.blankj.utilcode.util.ShellUtils

internal object FiveGUtils {

    private const val TAG = "FiveGUtils"
    private val KEY_FUE = "Zml2ZWdfdXNlcl9lbmFibGU=".convertRuntimeName()

    init {
        System.loadLibrary("fivegswitcher")
    }

    val isFiveGCapable by lazy {
        runCatching {
            ReflectUtils.reflect(className)
                .method(method1)
                .method(method2)
                .get() as Boolean
        }.onFailure {
            Log.e(TAG, method2, it)
        }.getOrDefault(false)
    }
    val isDeviceRooted by lazy { DeviceUtils.isDeviceRooted() }
    val modes: Array<String> by lazy { FSApp.getContext().resources.getStringArray(R.array.entry_values) }

    private val consts by lazy { getConsts().split(";") }
    private val method1 by lazy { consts[1].convertRuntimeName() }
    private val method3 by lazy { consts[3].convertRuntimeName() }
    private val method2 by lazy { consts[2].convertRuntimeName() }
    private val method4 by lazy { consts[4].convertRuntimeName() }
    private val className by lazy { consts[0].convertRuntimeName() }

    fun isUserFiveGEnabled() = runCatching {
        ReflectUtils.reflect(className)
            .method(method1)
            .method(method4)
            .get() as Boolean
    }.onFailure {
        Log.e(TAG, method4, it)
    }.getOrDefault(
        runCatching {
            Settings.Global.getInt(
                FSApp.getContext().contentResolver,
                KEY_FUE
            ) == 1
        }.onFailure {
            Log.e(TAG, KEY_FUE, it)
        }.getOrDefault(false)
    )

    fun check5GEnabledInNormal() = runCatching {
        val test = !isUserFiveGEnabled()
        if (set5GEnabledInNormal(test)) {
            set5GEnabledInNormal(!test)
            true
        } else {
            false
        }
    }.getOrDefault(false)

    fun setUserFiveGEnabled(enable: Boolean): Boolean = runCatching {
        when (getMode()) {
            modes[1] -> {
                set5GEnabledInShizuku(enable)
            }
            modes[2] -> {
                set5GEnabledInRoot(enable)
            }
            else -> {
                set5GEnabledInNormal(enable)
                        || set5GEnabledInRoot(enable)
                        || set5GEnabledInShizuku(enable)
            }
        }
    }.onSuccess {
        if (!it) R.string.toast_pls_check_perm.showToastLong()
    }.onFailure {
        Log.e(TAG, "$method3 $enable", it)
        R.string.toast_pls_check_perm.showToastLong()
    }.getOrDefault(false)

    fun isAppRooted() = isDeviceRooted && AppUtils.isAppRoot()

    fun String.convertRuntimeName() = String(Base64.decode(this, Base64.DEFAULT))

    private external fun getConsts(): String

    private fun getMode() = PreferenceManager.getDefaultSharedPreferences(FSApp.getContext())
        .getString(MoreBottomSheetFragment.SettingsFragment.SP_KEY_SELECT_MODE, modes[0])

    private fun set5GEnabledInNormal(enable: Boolean) = runCatching {
        ReflectUtils.reflect(className)
            .method(method1)
            .method(method3, enable)
        enable == isUserFiveGEnabled()
    }.getOrDefault(false)

    private fun set5GEnabledInShizuku(enable: Boolean) = runCatching {
        Settings.Global.putInt(
            FSApp.getContext().contentResolver,
            KEY_FUE,
            if (enable) 1 else 0
        )
    }.getOrDefault(false)

    private fun set5GEnabledInRoot(enable: Boolean) = runCatching {
        execCmdRoot("settings put global $KEY_FUE ${if (enable) 1 else 0}")
    }.getOrDefault(false)

    private fun execCmdRoot(cmd: String) = if (isAppRooted()) {
        ShellUtils.execCmd(cmd, true).result == 0
    } else {
        false
    }
}
