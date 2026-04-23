package com.simplifybiz.mobile

import android.app.Application
import com.simplifybiz.mobile.di.initKoin
import org.koin.android.ext.koin.androidContext

class SimplifyBizApplication : Application() {
    override fun onCreate() {
        super.onCreate()


        initKoin {
            androidContext(this@SimplifyBizApplication)
        }
    }
}
