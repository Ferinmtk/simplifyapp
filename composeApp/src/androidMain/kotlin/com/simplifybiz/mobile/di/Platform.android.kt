package com.simplifybiz.mobile.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.simplifybiz.mobile.data.AppDatabase
import org.koin.mp.KoinPlatform.getKoin

internal actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context: Context = getKoin().get()
    val dbFile = context.getDatabasePath("simplifybiz.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}
