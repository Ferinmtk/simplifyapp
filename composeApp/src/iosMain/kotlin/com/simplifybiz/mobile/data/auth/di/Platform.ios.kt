package com.simplifybiz.mobile.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.simplifybiz.mobile.data.AppDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSHomeDirectory

/**
 * iOS Room database builder.
 *
 * The DB file lives under the app sandbox's Documents directory. Room's KMP
 * databaseBuilder takes just `name` on Native — the generated
 * `instantiateImpl` does the rest. The factory parameter is Android-only.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = NSHomeDirectory() + "/Documents/simplifybiz.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    )
}