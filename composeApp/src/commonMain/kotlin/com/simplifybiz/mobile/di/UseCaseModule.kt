package com.simplifybiz.mobile.di

import com.simplifybiz.mobile.data.MoneyRepository
import com.simplifybiz.mobile.domain.DeleteObjectiveUseCase
import com.simplifybiz.mobile.domain.GetLeadershipUseCase
import com.simplifybiz.mobile.domain.GetMarketingUseCase
import com.simplifybiz.mobile.domain.GetMoneyUseCase
import com.simplifybiz.mobile.domain.GetObjectiveUseCase
import com.simplifybiz.mobile.domain.GetOperationsUseCase
import com.simplifybiz.mobile.domain.GetPeopleUseCase
import com.simplifybiz.mobile.domain.GetResearchAndDevelopmentUseCase
import com.simplifybiz.mobile.domain.GetRiskUseCase
import com.simplifybiz.mobile.domain.GetSalesUseCase
import com.simplifybiz.mobile.domain.GetStrategyUseCase
import com.simplifybiz.mobile.domain.UpdateLeadershipUseCase
import com.simplifybiz.mobile.domain.UpdateMarketingUseCase
import com.simplifybiz.mobile.domain.UpdateMoneyUseCase
import com.simplifybiz.mobile.domain.UpdateObjectiveUseCase
import com.simplifybiz.mobile.domain.UpdateOperationsUseCase
import com.simplifybiz.mobile.domain.UpdatePeopleUseCase
import com.simplifybiz.mobile.domain.UpdateResearchAndDevelopmentUseCase
import com.simplifybiz.mobile.domain.UpdateRiskUseCase
import com.simplifybiz.mobile.domain.UpdateSalesUseCase
import com.simplifybiz.mobile.domain.UpdateStrategyUseCase
import com.simplifybiz.mobile.domain.GetLinksUseCase
import org.koin.dsl.module

val useCaseModule = module {

    factory { GetStrategyUseCase(repository = get()) }
    factory { UpdateStrategyUseCase(repository = get()) }

    factory { GetLeadershipUseCase(repository = get()) }
    factory { UpdateLeadershipUseCase(repository = get()) }

    factory { GetMarketingUseCase(repository = get()) }
    factory { UpdateMarketingUseCase(repository = get()) }

    factory { GetObjectiveUseCase(repository = get()) }
    factory { UpdateObjectiveUseCase(repository = get()) }
    factory { DeleteObjectiveUseCase(repository = get()) }

    factory { GetResearchAndDevelopmentUseCase(repository = get()) }
    factory { UpdateResearchAndDevelopmentUseCase(repository = get()) }

    factory { GetRiskUseCase(repository = get()) }
    factory { UpdateRiskUseCase(repository = get()) }

    factory { GetSalesUseCase(repository = get()) }
    factory { UpdateSalesUseCase(repository = get()) }

    factory { GetOperationsUseCase(repository = get()) }
    factory { UpdateOperationsUseCase(repository = get()) }

    factory { GetPeopleUseCase(repository = get()) }
    factory { UpdatePeopleUseCase(repository = get()) }

    factory { GetMoneyUseCase(repository = get<MoneyRepository>()) }
    factory { UpdateMoneyUseCase(repository = get<MoneyRepository>()) }

    // Links — pull only, no UpdateLinksUseCase
    factory { GetLinksUseCase(repository = get()) }

}