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
    }

    private fun startMNSettings() {
        startActivity(Intent("android.settings.NETWORK_OPERATOR_SETTINGS").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        finish()
    }

    override fun finish() {
        super.finish()
        thread {
            Thread.sleep(2000)
            FSApp.killSelf()
        }
    }
}
