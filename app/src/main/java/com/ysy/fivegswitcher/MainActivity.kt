package com.ysy.fivegswitcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startMNSettings()

        finish()
        thread {
            Thread.sleep(1000)
            FSApp.killSelf()
        }
    }

    private fun startMNSettings() {
        startActivity(Intent("android.settings.NETWORK_OPERATOR_SETTINGS"))
    }
}
