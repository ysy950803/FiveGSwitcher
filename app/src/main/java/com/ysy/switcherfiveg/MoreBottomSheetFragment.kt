package com.ysy.switcherfiveg

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.DialogPreference
import androidx.preference.EditTextPreference
import androidx.preference.EditTextPreferenceDialogFragmentCompat
import androidx.preference.ListPreference
import androidx.preference.ListPreferenceDialogFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.blankj.utilcode.util.ThreadUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class MoreBottomSheetFragment : BaseBottomDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_more_bottom_sheet, container, false)
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

            // Same tag androidx.preference uses, so only one dialog shows at a time.
            private const val DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG"

            val SP_KEY_MAIN_TITLE by lazy { FSApp.getContext().getString(R.string.main_title) }
            val SP_KEY_ENABLE_5G by lazy { FSApp.getContext().getString(R.string.enable_5g) }
            val SP_KEY_SELECT_MODE by lazy { FSApp.getContext().getString(R.string.select_mode) }
            val SP_KEY_TO_MNS by lazy {
                FSApp.getContext().getString(R.string.to_mobile_net_settings)
            }
            val SP_KEY_TO_NTS by lazy {
                FSApp.getContext().getString(R.string.to_network_type_settings)
            }
            val SP_KEY_EDIT_LABEL by lazy {
                FSApp.getContext().getString(R.string.edit_label)
            }
            val SP_KEY_LOVE_SUPPORT by lazy { FSApp.getContext().getString(R.string.love_support) }
            val SP_KEY_DEVELOPER_HOME by lazy {
                FSApp.getContext().getString(R.string.developer_home)
            }

            var isShowing = false
        }

        private val m5GSupport by lazy { FiveGUtils.isFiveGCapable }
        private val m5GEnabledInNormal by lazy { FiveGUtils.check5GEnabledInNormal() }
        private var mEnable5GReceiver: BroadcastReceiver? = null

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
                    if (m5GEnabledInNormal) {
                        FiveGUtils.setUserFiveGEnabled(newValue as Boolean)
                    } else {
                        context.tryStartActivity(Intent().apply {
                            component = ComponentName(
                                "Y29tLmFuZHJvaWQucGhvbmU=".convertRuntimeName(),
                                "Y29tLmFuZHJvaWQucGhvbmUuc2V0dGluZ3MuTWl1aUZpdmVHTmV0d29ya1NldHRpbmc=".convertRuntimeName()
                            )
                        }) {
                            context.tryStartActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
                                `package` = "com.android.settings"
                            })
                        }
                        true
                    }
                }
                isChecked = FiveGUtils.isUserFiveGEnabled()
                isEnabled = m5GSupport
                if (!isEnabled) return@apply
                LocalBroadcastManager.getInstance(requireContext())
                    .registerReceiver(object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            if (intent?.hasExtra(EXTRA_KEY_ENABLE_5G) == true) {
                                isChecked = intent.getBooleanExtra(EXTRA_KEY_ENABLE_5G, false)
                            }
                        }
                    }.also { mEnable5GReceiver = it }, IntentFilter(TAG))
            }

            findPreference<ListPreference>(SP_KEY_SELECT_MODE)?.apply {
                isEnabled = m5GSupport && m5GEnabledInNormal
                if (!isEnabled) {
                    isVisible = false
                    return@apply
                }
                isVisible = true
                if (value.isNullOrEmpty()) setValueIndex(0)

                fun onClickMode(newValue: String): Boolean {
                    when (newValue) {
                        // auto
                        FiveGUtils.modes[0] -> {
                            if (FiveGUtils.check5GEnabledInNormal()) {
                                // Not need Shizuku or Root
                                return true
                            }
                            if (FiveGUtils.isAppRooted()) {
                                // If app is rooted, not need Shizuku
                                return true
                            }
                            if (FiveGUtils.isDeviceRooted) {
                                showRootTipsDialog()
                            } else {
                                ShizukuUtils.onCreate(activity)
                            }
                        }
                        // shizuku
                        FiveGUtils.modes[1] -> {
                            return ShizukuUtils.onCreate(activity)
                        }
                        // root
                        FiveGUtils.modes[2] -> {
                            if (FiveGUtils.isDeviceRooted) {
                                if (!FiveGUtils.isAppRooted()) {
                                    showRootTipsDialog()
                                }
                            } else {
                                R.string.toast_not_support_root.showToastLong()
                                return false
                            }
                        }
                    }
                    return true
                }

                ThreadUtils.runOnUiThreadDelayed({
                    activity.runWithUIContext {
                        onClickMode(value)
                    }
                }, TimeUnit.SECONDS.toMillis(1))
                setOnPreferenceChangeListener { _, newValue -> onClickMode(newValue as String) }
            }

            findPreference<Preference>(SP_KEY_TO_MNS)?.setOnPreferenceClickListener {
                context.tryStartActivity(Intent("YW5kcm9pZC5zZXR0aW5ncy5ORVRXT1JLX09QRVJBVE9SX1NFVFRJTkdT".convertRuntimeName()).apply {
                    setClassName(
                        "Y29tLmFuZHJvaWQucGhvbmU=".convertRuntimeName(),
                        "Y29tLmFuZHJvaWQucGhvbmUuc2V0dGluZ3MuTW9iaWxlTmV0d29ya1NldHRpbmdz".convertRuntimeName()
                    )
                }) {
                    context.tryStartActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
                        `package` = "com.android.settings"
                    })
                }
                activity?.finish()
                true
            }

            findPreference<Preference>(SP_KEY_TO_NTS)?.setOnPreferenceClickListener {
                context.tryStartActivity(Intent().apply {
                    component = ComponentName(
                        "Y29tLmFuZHJvaWQucGhvbmU=".convertRuntimeName(),
                        "Y29tLmFuZHJvaWQucGhvbmUuc2V0dGluZ3MuUHJlZmVycmVkTmV0d29ya1R5cGVMaXN0UHJlZmVyZW5jZQ==".convertRuntimeName()
                    )
                }) {
                    context.tryStartActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
                        `package` = "com.android.settings"
                    })
                }
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
                if (FSApp.getContext().isCountryCN()) {
                    context.tryStartActivity(
                        "${"YWxpcGF5czovL3BsYXRmb3JtYXBpL3N0YXJ0YXBwP3NhSWQ9MTAwMDAwMDcmcXJjb2RlPQ==".convertRuntimeName()}${
                            URLEncoder.encode(
                                "aHR0cHM6Ly9xci5hbGlwYXkuY29tL2ZreDEyMzYyZGl1OTVvaDJhd2VhYWM1".convertRuntimeName(),
                                "UTF-8"
                            )
                        }"
                    ) {
                        context.tryStartActivity("aHR0cHM6Ly9xci5hbGlwYXkuY29tL2ZreDEyMzYyZGl1OTVvaDJhd2VhYWM1".convertRuntimeName())
                    }
                } else {
                    context.tryStartActivity("aHR0cHM6Ly9wYXlwYWwubWUveWFvc2hlbmd5dQ==".convertRuntimeName())
                }
                activity?.finish()
                true
            }

            findPreference<Preference>(SP_KEY_DEVELOPER_HOME)?.setOnPreferenceClickListener {
                context.tryStartActivity("aHR0cHM6Ly9wbGF5Lmdvb2dsZS5jb20vc3RvcmUvYXBwcy9kZXY/aWQ9NzIyMDUzMDExNjM4NDY1Nzk3Nw==".convertRuntimeName())
                activity?.finish()
                true
            }

            FSApp.putSettingsInitDone(true)
            isShowing = true
        }

        // Show ListPreference / EditTextPreference dialogs with a Material 3 alert
        // dialog (rounded corners, tonal surface, M3 buttons) instead of the legacy
        // AppCompat one. Falls back to the default for any other DialogPreference.
        override fun onDisplayPreferenceDialog(preference: Preference) {
            if (parentFragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) return
            val dialog: DialogFragment = when (preference) {
                is EditTextPreference -> M3EditTextPreferenceDialog()
                is ListPreference -> M3ListPreferenceDialog()
                else -> {
                    super.onDisplayPreferenceDialog(preference)
                    return
                }
            }
            dialog.arguments = Bundle(1).apply { putString("key", preference.key) }
            @Suppress("DEPRECATION")
            dialog.setTargetFragment(this, 0)
            dialog.show(parentFragmentManager, DIALOG_FRAGMENT_TAG)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            // Material 3 grouped-card look: drop the default row dividers and
            // give the list a little vertical breathing room.
            setDivider(null)
            setDividerHeight(0)
            val padV = resources.getDimensionPixelSize(R.dimen.m3_pref_list_padding_vertical)
            listView.apply {
                clipToPadding = false
                updatePadding(top = padV, bottom = padV)
                isVerticalScrollBarEnabled = false
            }
        }

        override fun onStart() {
            super.onStart()
            findPreference<SwitchPreferenceCompat>(SP_KEY_ENABLE_5G)?.isChecked =
                FiveGUtils.isUserFiveGEnabled()
        }

        override fun onDestroyView() {
            super.onDestroyView()
            isShowing = false
            mEnable5GReceiver?.let {
                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(it)
            }
            ShizukuUtils.onDestroy()
        }

        private fun showRootTipsDialog() = activity.runWithUIContext { ctx ->
            AlertDialog.Builder(ctx)
                .setMessage(R.string.dialog_pls_check_root_app)
                .setNegativeButton(R.string.dialog_pls_check_shizuku_cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }
    }
}

/**
 * Material 3 variant of the EditTextPreference dialog. Building the alert with
 * [MaterialAlertDialogBuilder] gives it the M3 look (rounded corners, tonal surface,
 * M3 buttons); the type-specific work — inflating/binding the text field and persisting
 * on positive — still runs through the framework hooks, so behavior is unchanged.
 *
 * NOTE: [onCreateDialogView]/[onBindDialogView]/[onPrepareDialogBuilder] are `protected`,
 * so this logic has to live inside the subclass rather than a shared helper.
 */
class M3EditTextPreferenceDialog : EditTextPreferenceDialogFragmentCompat() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val pref = preference as DialogPreference
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(pref.dialogTitle)
            .setIcon(pref.dialogIcon)
            .setPositiveButton(pref.positiveButtonText, this)
            .setNegativeButton(pref.negativeButtonText, this)
        val contentView = onCreateDialogView(requireContext())
        if (contentView != null) {
            onBindDialogView(contentView)
            builder.setView(contentView)
        } else {
            builder.setMessage(pref.dialogMessage)
        }
        onPrepareDialogBuilder(builder)
        return builder.create().also { dialog ->
            if (needInputMethod()) {
                dialog.window?.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                )
            }
        }
    }
}

/** Material 3 variant of the ListPreference (single-choice) dialog. */
class M3ListPreferenceDialog : ListPreferenceDialogFragmentCompat() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val pref = preference as DialogPreference
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(pref.dialogTitle)
            .setIcon(pref.dialogIcon)
            .setPositiveButton(pref.positiveButtonText, this)
            .setNegativeButton(pref.negativeButtonText, this)
        val contentView = onCreateDialogView(requireContext())
        if (contentView != null) {
            onBindDialogView(contentView)
            builder.setView(contentView)
        } else {
            builder.setMessage(pref.dialogMessage)
        }
        onPrepareDialogBuilder(builder)
        return builder.create()
    }
}
