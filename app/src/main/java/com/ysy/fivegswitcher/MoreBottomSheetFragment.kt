package com.ysy.fivegswitcher

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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

    override fun onStop() {
        super.onStop()
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().finish()
    }

    fun isDialogShowing(): Boolean = dialog?.isShowing == true

    class SettingsFragment : PreferenceFragmentCompat() {

        companion object {
            val SP_KEY_MAIN_TITLE by lazy { FSApp.getContext().getString(R.string.main_title) }
            val SP_KEY_TO_MNS by lazy {
                FSApp.getContext().getString(R.string.to_mobile_net_settings)
            }
            val SP_KEY_TO_AUTO_START by lazy {
                FSApp.getContext().getString(R.string.to_auto_start)
            }
            val SP_KEY_LOVE_SUPPORT by lazy { FSApp.getContext().getString(R.string.love_support) }
            val SP_KEY_DEVELOPER_HOME by lazy {
                FSApp.getContext().getString(R.string.developer_home)
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            findPreference<Preference>(SP_KEY_MAIN_TITLE)?.apply {
                title = getString(
                    if (FiveGUtils.isFiveGCapable()) R.string.five_g_tile_label
                    else R.string.settings_main_title_not_support
                )
            }

            findPreference<Preference>(SP_KEY_TO_MNS)?.setOnPreferenceClickListener {
                startActivity(Intent("android.settings.NETWORK_OPERATOR_SETTINGS").apply {
                    setClassName(
                        "com.android.phone",
                        "com.android.phone.settings.MobileNetworkSettings"
                    )
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                true
            }

            findPreference<Preference>(SP_KEY_TO_AUTO_START)?.setOnPreferenceClickListener {
                startActivity(Intent().apply {
                    setClassName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                true
            }

            findPreference<Preference>(SP_KEY_LOVE_SUPPORT)?.setOnPreferenceClickListener {
                try {
                    startActivity(
                        Intent.parseUri(
                            "alipays://platformapi/startapp?saId=10000007&qrcode=${
                                URLEncoder.encode(
                                    "https://qr.alipay.com/fkx12362diu95oh2aweaac5",
                                    "UTF-8"
                                )
                            }",
                            Intent.URI_INTENT_SCHEME
                        )
                    )
                } catch (e: Exception) {
                    startActivity(
                        Intent.parseUri(
                            "https://qr.alipay.com/fkx12362diu95oh2aweaac5",
                            Intent.URI_INTENT_SCHEME
                        )
                    )
                }
                true
            }

            findPreference<Preference>(SP_KEY_DEVELOPER_HOME)?.setOnPreferenceClickListener {
                try {
                    startActivity(
                        Intent.parseUri(
                            "coolmarket://u/4617184",
                            Intent.URI_INTENT_SCHEME
                        )
                    )
                } catch (e: Exception) {
                    startActivity(
                        Intent.parseUri(
                            "https://www.coolapk.com/u/4617184",
                            Intent.URI_INTENT_SCHEME
                        )
                    )
                }
                true
            }
        }
    }
}
