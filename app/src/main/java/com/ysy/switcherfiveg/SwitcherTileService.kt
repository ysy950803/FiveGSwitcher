package com.ysy.switcherfiveg

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.blankj.utilcode.util.IntentUtils

@SuppressLint("StartActivityAndCollapseDeprecated")
class SwitcherTileService : TileService() {

    companion object {
        private const val TAG = "SwitcherTileService"
    }

    private val m5GSupport by lazy { FiveGUtils.isFiveGCapable }
    private val m5GEnabledInNormal by lazy { FiveGUtils.check5GEnabledInNormal() }
    private var mActiveIcon: Icon? = null
    private var mInActiveIcon: Icon? = null

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Log.v(TAG, "attachBaseContext")
        mActiveIcon = mActiveIcon ?: Icon.createWithResource(this, R.drawable.ic_5g_white_24dp)
        mInActiveIcon = mInActiveIcon ?: Icon.createWithResource(this, R.drawable.ic_5g_white_24dp)
            .setTint(0x80FFFFFF.toInt())
    }

    override fun onStartListening() {
        super.onStartListening()
        Log.v(TAG, "onStartListening")
        if (m5GSupport) {
            updateTile(FiveGUtils.isUserFiveGEnabled())
        } else {
            qsTile?.apply {
                state = Tile.STATE_UNAVAILABLE
                updateTile()
            }
        }
    }

    override fun onClick() {
        super.onClick()
        Log.v(TAG, "onClick ${qsTile?.state}")
        if (!m5GSupport) {
            R.string.settings_main_title_not_support.showToastLong()
            return
        }
        if (!FSApp.isSettingsInitDone) {
            R.string.toast_settings_not_init.showToastLong()
            val intent = Intent(FSApp.getContext(), MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(
                    PendingIntent.getActivity(
                        FSApp.getContext(),
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            } else {
                startActivityAndCollapse(intent)
            }
            return
        }
        toggle()
    }

    private fun toggle() {
        if (m5GEnabledInNormal) {
            val newEnabled = !FiveGUtils.isUserFiveGEnabled()
            FiveGUtils.setUserFiveGEnabled(newEnabled)
            updateTile(newEnabled)
            if (MoreBottomSheetFragment.SettingsFragment.isShowing) {
                LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(MoreBottomSheetFragment.SettingsFragment.TAG).apply {
                        putExtra(
                            MoreBottomSheetFragment.SettingsFragment.EXTRA_KEY_ENABLE_5G,
                            newEnabled
                        )
                    })
            }
        } else {
            var intent = Intent().apply {
                component = ComponentName(
                    // com.android.phone
                    "Y29tLmFuZHJvaWQucGhvbmU=".convertRuntimeName(),
                    // com.android.phone.settings.PreferredNetworkTypeListPreference
                    "Y29tLmFuZHJvaWQucGhvbmUuc2V0dGluZ3MuUHJlZmVycmVkTmV0d29ya1R5cGVMaXN0UHJlZmVyZW5jZQ==".convertRuntimeName()
                )
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (!IntentUtils.isIntentAvailable(intent)) {
                intent = Intent().apply {
                    component = ComponentName(
                        // com.android.phone
                        "Y29tLmFuZHJvaWQucGhvbmU=".convertRuntimeName(),
                        // com.android.phone.settings.MiuiFiveGNetworkSetting
                        "Y29tLmFuZHJvaWQucGhvbmUuc2V0dGluZ3MuTWl1aUZpdmVHTmV0d29ya1NldHRpbmc=".convertRuntimeName()
                    )
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (!IntentUtils.isIntentAvailable(intent)) {
                    intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
                        // com.android.settings
                        `package` = "Y29tLmFuZHJvaWQuc2V0dGluZ3M=".convertRuntimeName()
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(
                    PendingIntent.getActivity(
                        FSApp.getContext(),
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            } else {
                startActivityAndCollapse(intent)
            }
        }
    }

    private fun updateTile(active: Boolean) {
        qsTile?.apply {
            icon = if (active) mActiveIcon else mInActiveIcon
            label = FSApp.getLabel() ?: getString(R.string.five_g_tile_label)
            state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        Log.v(TAG, "onStopListening")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.v(TAG, "onBind")
        return super.onBind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.v(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onTileAdded() {
        super.onTileAdded()
        Log.v(TAG, "onTileAdded")
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        Log.v(TAG, "onTileRemoved")
    }
}
