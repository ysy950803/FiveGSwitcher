package com.ysy.fivegswitcher

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Process
import me.weishu.reflection.Reflection
import kotlin.system.exitProcess

class FSApp : Application() {

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var CONTEXT: Context? = null

        fun getContext(): Context = CONTEXT!!

        fun killSelf() {
            Process.killProcess(Process.myPid())
            exitProcess(0)
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        CONTEXT = this
        Reflection.unseal(base)
    }
}
