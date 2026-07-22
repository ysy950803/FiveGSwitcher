package com.ysy.switcherfiveg

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import com.blankj.utilcode.util.ScreenUtils

fun Context?.isNotValid() = this !is Activity || this.isFinishing

fun Context?.isCountryCN() =
    (this?.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)?.simCountryIso == "cn"

inline fun Activity?.runWithUIContext(block: (Context) -> Unit) {
    this?.takeIf { !it.isNotValid() }?.let { block.invoke(it) }
}

fun Context?.tryStartActivity(intent: Intent?, onFailure: (() -> Unit)? = null) {
    runCatching {
        intent?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (this is Activity) this.startActivity(intent)
        else if (this is Application) this.startActivity(intent)
    }.onFailure {
        it.printStackTrace()
        onFailure?.invoke()
    }
}

fun Context?.tryStartActivity(uri: String, onFailure: (() -> Unit)? = null) {
    tryStartActivity(Intent.parseUri(uri, Intent.URI_INTENT_SCHEME)) { onFailure?.invoke() }
}

fun Int.showToastLong() {
    Toast.makeText(FSApp.getContext(), this, Toast.LENGTH_LONG).show()
}

fun String.convertRuntimeName() = String(Base64.decode(this, Base64.DEFAULT))

fun getScreenHeight(inApp: Boolean = true) =
    if (inApp) FSApp.getContext().resources.displayMetrics.heightPixels
    else ScreenUtils.getScreenHeight()

inline fun View?.applySystemBarsInsets(
    crossinline onApply: (v: View, insets: Insets) -> Unit = { _, _ -> }
) {
    this ?: return
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val systemBarSpacing = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        runCatching { onApply(v, systemBarSpacing) }
        insets
    }
}

fun View.postRunCatching(onFail: ((t: Throwable) -> Unit)? = null, block: () -> Unit) {
    this.post { runCatching { block() }.onFailure { onFail?.invoke(it) } }
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

        var isSettingsInitDone = false
            get() = field || PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(SP_KEY_SETTINGS_INIT_DONE, false).also { field = it }

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
