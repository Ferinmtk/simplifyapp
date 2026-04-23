package com.simplifybiz.mobile.di

import androidx.room.RoomDatabase
import com.simplifybiz.mobile.data.AppDatabase

/**
 * Platform-specific Room database builder.
 *
 * Android: uses the app Context (sourced from Koin) and writes the DB file
 *   into the app's files dir via context.getDatabasePath.
 * iOS: uses NSHomeDirectory() + /Documents/simplifybiz.db for the file path.
 *
 * The Platform interface / getPlatform() helper was removed — it was a
 * dead diagnostic leftover. If you need platform info, query UIDevice or
 * android.os.Build directly from the platform source set.
 */
internal expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>