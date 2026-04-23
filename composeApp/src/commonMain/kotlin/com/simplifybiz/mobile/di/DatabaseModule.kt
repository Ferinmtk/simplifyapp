package com.simplifybiz.mobile.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.simplifybiz.mobile.data.AppDatabase
import com.simplifybiz.mobile.data.SessionManager
import com.simplifybiz.mobile.data.auth.AuthRepository
import com.simplifybiz.mobile.data.auth.GoogleAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    single { SessionManager(get()) }
    single { GoogleAuthManager(androidContext()) }
    single { AuthRepository(get(), get(), get()) }

    single {
        val builder: RoomDatabase.Builder<AppDatabase> = getDatabaseBuilder()
        builder
            .fallbackToDestructiveMigration()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single { get<AppDatabase>().strategyDao() }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().leadershipDao() }
    single { get<AppDatabase>().marketingDao() }
    single { get<AppDatabase>().objectiveDao() }
    single { get<AppDatabase>().researchAndDevelopmentDao() }
    single { get<AppDatabase>().riskDao() }
    single { get<AppDatabase>().salesDao() }
    single { get<AppDatabase>().operationsDao() }
    single { get<AppDatabase>().peopleDao() }
    single { get<AppDatabase>().moneyDao() }
    single { get<AppDatabase>().linkDao() }
}