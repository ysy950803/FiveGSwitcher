package com.ysy.fivegswitcher

import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import kotlin.concurrent.thread

@RequiresApi(Build.VERSION_CODES.N)
class SwitcherTileService : TileService() {

    private var mActiveIcon: Icon? = null
    private var mInActiveIcon: Icon? = null
    private var mFiveGSupport = false

    // optimization for battery
    private var mKillingSelf = false
    private val mKillSelfRunnable = Runnable {
        if (mKillingSelf) return@Runnable
        synchronized(mKillingSelf) {
            if (mKillingSelf) return@Runnable
            mKillingSelf = true
            Thread.sleep(5000)
            FSApp.killSelf()
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        mActiveIcon = mActiveIcon ?: Icon.createWithResource(this, R.drawable.ic_5g_white_24dp)
        mInActiveIcon = mInActiveIcon ?: Icon.createWithResource(this, R.drawable.ic_5g_white_24dp)
            .setTint(0x80FFFFFF.toInt())
        mFiveGSupport = if (mFiveGSupport) true else FiveGUtils.isFiveGCapable()
    }

    override fun onStartListening() {
        super.onStartListening()
        if (mFiveGSupport) {
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
        if (!mFiveGSupport) return
        if (isLocked) {
            unlockAndRun { toggle() }
        } else {
            toggle()
        }
    }

    private fun toggle() {
        val enabled = FiveGUtils.isUserFiveGEnabled()
        FiveGUtils.setUserFiveGEnabled(!enabled)
        updateTile(!enabled)
    }

    private fun updateTile(active: Boolean) {
        qsTile?.apply {
            icon = if (active) mActiveIcon else mInActiveIcon
            label = getString(R.string.five_g_tile_label)
            state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        stopSelf()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        stopSelf()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        thread { mKillSelfRunnable.run() }
    }
}
