package com.ysy.fivegswitcher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    companion object {
        // am start -n com.ysy.fivegswitcher/.MainActivity --ez enable_5g true
        private const val EXT_KEY_ENABLE_5G = "enable_5g"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.hasExtra(EXT_KEY_ENABLE_5G) == true) {
            FiveGUtils.setUserFiveGEnabled(intent.getBooleanExtra(EXT_KEY_ENABLE_5G, false))
        } else {
            startMNSettings()
        }
        finish()
    }

    private fun startMNSettings() {
        startActivity(Intent("android.settings.NETWORK_OPERATOR_SETTINGS").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    override fun finish() {
        super.finish()
        thread {
            Thread.sleep(2000)
            FSApp.killSelf()
        }
    }
}
