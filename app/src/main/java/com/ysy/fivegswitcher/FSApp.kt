package com.ysy.fivegswitcher

import android.app.Application
import android.content.Context
import me.weishu.reflection.Reflection

class FSApp : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Reflection.unseal(base)
    }
}
