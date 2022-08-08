package com.ysy.switcherfiveg

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager

fun Context?.isNotValid() = this !is Activity || this.isFinishing

fun Context?.isCountryCN() =
    (this?.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)?.simCountryIso == "cn"

inline fun Activity?.runWithUIContext(block: (Context) -> Unit) {
    this?.takeIf { !it.isNotValid() }?.let { block.invoke(it) }
}

fun Fragment.tryStartActivity(intent: Intent?, onFailure: (() -> Unit)? = null) {
    runCatching {
        this.startActivity(intent?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }.onFailure {
        it.printStackTrace()
        onFailure?.invoke()
    }
}

fun Context.tryStartActivity(intent: Intent?) {
    runCatching {
        if (this is Activity) this.startActivity(intent)
        else if (this is Application) this.startActivity(intent?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }.onFailure {
        it.printStackTrace()
    }
}

class FSApp : Application() {

    companion object {

        private const val SP_KEY_SETTINGS_INIT_DONE = "sp_key_settings_init_done"

        @SuppressLint("StaticFieldLeak")
        private var CONTEXT: Context? = null
        private var LABEL: String? = null

        fun getContext(): Context = CONTEXT!!

        fun setLabel(label: String) {
            LABEL = label
        }

        fun getLabel() = LABEL

        fun isSettingsInitDone() = PreferenceManager.getDefaultSharedPreferences(getContext())
            .getBoolean(SP_KEY_SETTINGS_INIT_DONE, false)

        fun putSettingsInitDone(initDone: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit {
                putBoolean(SP_KEY_SETTINGS_INIT_DONE, initDone)
            }
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        CONTEXT = this
        initLabel()
    }

    private fun initLabel() {
        val label = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(
                MoreBottomSheetFragment.SettingsFragment.SP_KEY_EDIT_LABEL,
                null
            ) ?: getString(R.string.five_g_tile_label)
        setLabel(label)
    }
}
