package com.ysy.switcherfiveg

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ysy.switcherfiveg.FiveGUtils.convertRuntimeName
import java.net.URLEncoder

class MoreBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_more_bottom_sheet, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.DialogTheme)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }

    fun isDialogShowing(): Boolean = dialog?.isShowing == true

    class SettingsFragment : PreferenceFragmentCompat() {

        companion object {
            const val TAG = "SettingsFragment"
            const val EXTRA_KEY_ENABLE_5G = "enable_5g"

            val SP_KEY_MAIN_TITLE by lazy { FSApp.getContext().getString(R.string.main_title) }
            val SP_KEY_ENABLE_5G by lazy { FSApp.getContext().getString(R.string.enable_5g) }
            val SP_KEY_TO_MNS by lazy {
                FSApp.getContext().getString(R.string.to_mobile_net_settings)
            }
            val SP_KEY_EDIT_LABEL by lazy {
                FSApp.getContext().getString(R.string.edit_label)
            }
            val SP_KEY_LOVE_SUPPORT by lazy { FSApp.getContext().getString(R.string.love_support) }
            val SP_KEY_DEVELOPER_HOME by lazy {
                FSApp.getContext().getString(R.string.developer_home)
            }
        }

        private val m5GSupport by lazy { FiveGUtils.isFiveGCapable() }
        private var mEnable5GReceiver: BroadcastReceiver? = null

        private fun tryStartActivity(intent: Intent?) {
            try {
                super.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            findPreference<Preference>(SP_KEY_MAIN_TITLE)?.apply {
                title = getString(
                    if (m5GSupport) R.string.five_g_tile_label
                    else R.string.settings_main_title_not_support
                )
            }

            findPreference<SwitchPreferenceCompat>(SP_KEY_ENABLE_5G)?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    FiveGUtils.setUserFiveGEnabled(newValue as Boolean)
                    true
                }
                isChecked = FiveGUtils.isUserFiveGEnabled()
                isEnabled = m5GSupport
                if (m5GSupport) {
                    LocalBroadcastManager.getInstance(requireContext())
                        .registerReceiver(object : BroadcastReceiver() {
                            override fun onReceive(context: Context?, intent: Intent?) {
                                if (intent?.hasExtra(EXTRA_KEY_ENABLE_5G) == true) {
                                    isChecked = intent.getBooleanExtra(EXTRA_KEY_ENABLE_5G, false)
                                }
                            }
                        }.also { mEnable5GReceiver = it }, IntentFilter(TAG))
                }
            }

            findPreference<Preference>(SP_KEY_TO_MNS)?.setOnPreferenceClickListener {
                tryStartActivity(Intent("YW5kcm9pZC5zZXR0aW5ncy5ORVRXT1JLX09QRVJBVE9SX1NFVFRJTkdT".convertRuntimeName()).apply {
                    setClassName(
                        "Y29tLmFuZHJvaWQucGhvbmU=".convertRuntimeName(),
                        "Y29tLmFuZHJvaWQucGhvbmUuc2V0dGluZ3MuTW9iaWxlTmV0d29ya1NldHRpbmdz".convertRuntimeName()
                    )
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                activity?.finish()
                true
            }

            findPreference<EditTextPreference>(SP_KEY_EDIT_LABEL)?.setOnPreferenceChangeListener { _, newValue ->
                val label = newValue as? String ?: ""
                when {
                    label.isBlank() -> FSApp.setLabel(getString(R.string.five_g_tile_label))
                    label.length > 6 -> return@setOnPreferenceChangeListener false
                    else -> FSApp.setLabel(label)
                }
                return@setOnPreferenceChangeListener true
            }

            findPreference<Preference>(SP_KEY_LOVE_SUPPORT)?.setOnPreferenceClickListener {
                try {
                    startActivity(
                        Intent.parseUri(
                            "${"YWxpcGF5czovL3BsYXRmb3JtYXBpL3N0YXJ0YXBwP3NhSWQ9MTAwMDAwMDcmcXJjb2RlPQ==".convertRuntimeName()}${
                                URLEncoder.encode(
                                    "aHR0cHM6Ly9xci5hbGlwYXkuY29tL2ZreDEyMzYyZGl1OTVvaDJhd2VhYWM1".convertRuntimeName(),
                                    "UTF-8"
                                )
                            }",
                            Intent.URI_INTENT_SCHEME
                        )
                    )
                } catch (e: Exception) {
                    tryStartActivity(
                        Intent.parseUri(
                            "aHR0cHM6Ly9xci5hbGlwYXkuY29tL2ZreDEyMzYyZGl1OTVvaDJhd2VhYWM1".convertRuntimeName(),
                            Intent.URI_INTENT_SCHEME
                        )
                    )
                } finally {
                    activity?.finish()
                }
                true
            }

            findPreference<Preference>(SP_KEY_DEVELOPER_HOME)?.setOnPreferenceClickListener {
                try {
                    startActivity(
                        Intent.parseUri(
                            "Y29vbG1hcmtldDovL3UvNDYxNzE4NA==".convertRuntimeName(),
                            Intent.URI_INTENT_SCHEME
                        )
                    )
                } catch (e: Exception) {
                    tryStartActivity(
                        Intent.parseUri(
                            "aHR0cHM6Ly93d3cuY29vbGFway5jb20vdS80NjE3MTg0".convertRuntimeName(),
                            Intent.URI_INTENT_SCHEME
                        )
                    )
                } finally {
                    activity?.finish()
                }
                true
            }
        }

        override fun onDestroyView() {
            mEnable5GReceiver?.let {
                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(it)
            }
            super.onDestroyView()
        }
    }
}
