package com.simplifybiz.mobile.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.simplifybiz.mobile.data.AppDatabase
import org.koin.mp.KoinPlatform.getKoin

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

// Actual implementation: specific to Android
internal actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    // We ask Koin for the Android Context (we'll setup Koin next)
    val context: Context = getKoin().get()

    val dbFile = context.getDatabasePath("simplifybiz.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}
