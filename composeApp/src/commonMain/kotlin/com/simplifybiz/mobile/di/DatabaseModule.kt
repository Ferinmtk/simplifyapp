package com.simplifybiz.mobile.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.simplifybiz.mobile.data.AppDatabase
import com.simplifybiz.mobile.data.SessionManager
import com.simplifybiz.mobile.data.auth.AuthRepository
import com.simplifybiz.mobile.data.auth.GoogleAuthManager
import com.simplifybiz.mobile.util.ioDispatcher
import org.koin.dsl.module

val databaseModule = module {

    single { SessionManager(get()) }

    // GoogleAuthManager is an `expect class` — Android's actual takes a
    // Context, iOS's actual takes none. Koin's DSL resolves each from the
    // platform's own Koin module (see KoinAndroid.kt / KoinIos.kt), so we
    // no longer reference androidContext() from commonMain.
    single { AuthRepository(get(), get(), get()) }

    single {
        val builder: RoomDatabase.Builder<AppDatabase> = getDatabaseBuilder()
        builder
            .fallbackToDestructiveMigration(dropAllTables = true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(ioDispatcher)
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