package com.ysy.fivegswitcher

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager

class FSApp : Application() {

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var CONTEXT: Context? = null

        private var LABEL: String? = null

        fun getContext(): Context = CONTEXT!!

        fun setLabel(label: String) {
            LABEL = label
        }

        fun getLabel() = LABEL
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
