package com.simplifybiz.mobile.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserEntity::class,
        StrategyEntity::class,
        LeadershipEntity::class,
        MarketingEntity::class,
        ObjectiveEntity::class,
        ResearchAndDevelopmentEntity::class,
        RiskEntity::class,
        SalesEntity::class,
        OperationsEntity::class,
        PeopleEntity::class,
        MoneyEntity::class,
        LinkEntity::class,
    ],
    // Bumped 27 -> 28: added deleted_system_remote_ids column on leadership.
    // Bumped 28 -> 29: added deleted_system_remote_ids column on marketing.
    // Bumped 29 -> 30: added deleted_system_remote_ids column on sales.
    // Bumped 30 -> 31: added deleted_system_remote_ids column on operations.
    // Bumped 31 -> 32: added deleted_system_remote_ids column on people, dropped
    //                  orphan `programs` column from people.
    // Bumped 32 -> 33: money module port. Added deleted_system_remote_ids column,
    //                  renamed file upload columns to the _url suffix
    //                  (annual_budget_url, departmental_budget_url,
    //                  cash_flow_forecast_url), added new financial_reports
    //                  column, and added @ColumnInfo-backed snake_case for
    //                  previously implicit column names.
    // Bumped 33 -> 34: risk + R&D systems port. On both tables:
    //                   - dropped legacy `system_items` column (stored the old
    //                     SystemItem JSON blob which the server never received
    //                     because toSyncPayloads stripped it anyway),
    //                   - added `systems_used` column (List<LeadershipSystemItem>),
    //                   - added `deleted_system_remote_ids` column (List<Int>).
    //                  Also removed the standalone SystemItem type — Risk and
    //                  R&D reuse LeadershipSystemItem now, matching Sales /
    //                  Operations.
    // fallbackToDestructiveMigration() in DatabaseModule will wipe local data
    // on first launch post-upgrade — acceptable pre-release, fix before shipping.
    version = 35
)
@TypeConverters(Converters::class)
internal abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun strategyDao(): StrategyDao
    abstract fun leadershipDao(): LeadershipDao
    abstract fun marketingDao(): MarketingDao
    abstract fun objectiveDao(): ObjectiveDao
    abstract fun researchAndDevelopmentDao(): ResearchAndDevelopmentDao
    abstract fun riskDao(): RiskDao
    abstract fun salesDao(): SalesDao
    abstract fun operationsDao(): OperationsDao
    abstract fun peopleDao(): PeopleDao
    abstract fun moneyDao(): MoneyDao
    abstract fun linkDao(): LinkDao

}