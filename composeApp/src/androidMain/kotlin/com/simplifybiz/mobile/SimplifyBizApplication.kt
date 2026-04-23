package com.simplifybiz.mobile

import android.app.Application
import com.simplifybiz.mobile.di.androidPlatformModule
import com.simplifybiz.mobile.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules

class SimplifyBizApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@SimplifyBizApplication)
        }

        // Platform-only bindings (GoogleAuthManager needs Context)
        loadKoinModules(androidPlatformModule)
    }
}