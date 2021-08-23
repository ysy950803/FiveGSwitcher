package com.ysy.fivegswitcher

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class SwitcherTileService : TileService() {

    companion object {
        private const val TAG = "SwitcherTileService"
    }

    private var mActiveIcon: Icon? = null
    private var mInActiveIcon: Icon? = null
    private val mFiveGSupport by lazy { FiveGUtils.isFiveGCapable() }

    private val mRunnableQueue = LinkedBlockingQueue<Runnable>(1)

    private fun tryToKillSelf() {
        if (mRunnableQueue.size >= 1) return
        mRunnableQueue.offer(Runnable { FSApp.killSelf() })
        thread {
            Thread.sleep(2000)
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
            label = FSApp.getLabel() ?: getString(R.string.five_g_tile_label)
            state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }


    override fun onStopListening() {
        super.onStopListening()
        Log.d(TAG, "onStopListening")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        return super.onBind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)
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
