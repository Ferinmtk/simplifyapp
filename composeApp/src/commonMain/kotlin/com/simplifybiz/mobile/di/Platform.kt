package com.simplifybiz.mobile.di

import androidx.room.RoomDatabase
import com.simplifybiz.mobile.data.AppDatabase

interface Platform {
    val name: String
}

// We "expect" the platform (Android) to provide a builder for us
internal expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
