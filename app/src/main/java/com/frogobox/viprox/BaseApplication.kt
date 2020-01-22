package com.frogobox.viprox

import android.app.Application
import android.content.Context

class BaseApplication : Application() {

    companion object {
        lateinit var instance: BaseApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    fun getResourceString(resId: Int): String {
        return instance.getString(resId)
    }

}