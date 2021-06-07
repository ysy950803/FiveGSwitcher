package com.ysy.fivegswitcher

import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

@RequiresApi(Build.VERSION_CODES.N)
class SwitcherTileService : TileService() {

    companion object {
        private const val TAG = "SwitcherTileService"
    }

    private var mActiveIcon: Icon? = null
    private var mInActiveIcon: Icon? = null
    private var mFiveGSupport = false

    // Optimization for battery
    private val mRunnableQueue = LinkedBlockingQueue<Runnable>(1)

    private fun tryToKillSelf() {
        if (mRunnableQueue.size >= 1) return
        mRunnableQueue.offer(Runnable { FSApp.killSelf() })
        thread {
            Thread.sleep(5000)
            Log.d(TAG, "killSelf done")
            mRunnableQueue.poll()?.run()
        }
    }

    private fun cancelKillingSelf() {
        mRunnableQueue.poll()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Log.d(TAG, "attachBaseContext")
        mActiveIcon = mActiveIcon ?: Icon.createWithResource(this, R.drawable.ic_5g_white_24dp)
        mInActiveIcon = mInActiveIcon ?: Icon.createWithResource(this, R.drawable.ic_5g_white_24dp)
            .setTint(0x80FFFFFF.toInt())
        mFiveGSupport = if (mFiveGSupport) true else FiveGUtils.isFiveGCapable()
    }

    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "onStartListening")
        cancelKillingSelf()
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
        Log.d(TAG, "onClick ${qsTile?.state}")
        cancelKillingSelf()
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
        Log.d(TAG, "onStopListening")
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        tryToKillSelf()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        Log.d(TAG, "onTileAdded")
        cancelKillingSelf()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        Log.d(TAG, "onTileRemoved")
        tryToKillSelf()
    }
}
