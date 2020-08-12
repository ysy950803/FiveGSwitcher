package com.ysy.fivegswitcher

import android.app.Application
import android.content.Context
import android.os.Process
import me.weishu.reflection.Reflection
import kotlin.system.exitProcess

class FSApp : Application() {

    companion object {

        fun killSelf() {
            Process.killProcess(Process.myPid())
            exitProcess(0)
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Reflection.unseal(base)
    }
}
