package com.simplifybiz.mobile.di

import com.russhwolf.settings.Settings
import com.simplifybiz.mobile.data.LeadershipRepository
import com.simplifybiz.mobile.data.MarketingRepository
import com.simplifybiz.mobile.data.ObjectiveRepository
import com.simplifybiz.mobile.data.OperationsRepository
import com.simplifybiz.mobile.data.ResearchAndDevelopmentRepository
import com.simplifybiz.mobile.data.RiskRepository
import com.simplifybiz.mobile.data.SalesRepository
import com.simplifybiz.mobile.data.StrategyRepository
import com.simplifybiz.mobile.data.PeopleRepository
import com.simplifybiz.mobile.data.MoneyRepository
import com.simplifybiz.mobile.data.LinkRepository
import com.simplifybiz.mobile.data.UniversalSyncRepository
import org.koin.dsl.module

val repositoryModule = module {

    single { Settings() }

    single {
        UniversalSyncRepository(
            client = get(),
            strategyDao = get(),
            leadershipDao = get(),
            marketingDao = get(),
            salesDao = get(),
            operationsDao = get(),
            peopleDao = get(),
            objectiveDao = get(),
            researchAndDevelopmentDao = get(),
            riskDao = get(),
            moneyDao = get(),
            linkDao = get(),
        )
    }

    single { StrategyRepository(get()) }
    single { LeadershipRepository(get()) }
    single { MarketingRepository(get()) }
    single { SalesRepository(get()) }
    single { OperationsRepository(get()) }
    single { PeopleRepository(get()) }
    single { MoneyRepository(get()) }
    single { ObjectiveRepository(get()) }
    single { ResearchAndDevelopmentRepository(get()) }
    single { RiskRepository(get()) }
    single { LinkRepository(get()) }
}