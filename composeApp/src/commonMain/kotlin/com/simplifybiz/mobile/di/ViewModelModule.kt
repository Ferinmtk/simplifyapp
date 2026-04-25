package com.simplifybiz.mobile.di

import com.simplifybiz.mobile.presentation.DashboardViewModel
import com.simplifybiz.mobile.presentation.LeadershipViewModel
import com.simplifybiz.mobile.presentation.LoginViewModel
import com.simplifybiz.mobile.presentation.MarketingViewModel
import com.simplifybiz.mobile.presentation.MoneyViewModel
import com.simplifybiz.mobile.presentation.ObjectivesListViewModel
import com.simplifybiz.mobile.presentation.ObjectivesViewModel
import com.simplifybiz.mobile.presentation.OperationsViewModel
import com.simplifybiz.mobile.presentation.PeopleViewModel
import com.simplifybiz.mobile.presentation.ResearchAndDevelopmentViewModel
import com.simplifybiz.mobile.presentation.RiskViewModel
import com.simplifybiz.mobile.presentation.SalesViewModel
import com.simplifybiz.mobile.presentation.StrategyViewModel
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
import com.simplifybiz.mobile.domain.UpdateMoneyUseCase
import com.simplifybiz.mobile.presentation.LinksViewModel
import com.simplifybiz.mobile.domain.GetLinksUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel<LoginViewModel> { LoginViewModel(get(), get()) }

    viewModel {
        DashboardViewModel(
            syncRepository = get(),
            userDao = get(),
            sessionManager = get(),
            getStrategyUseCase = get<GetStrategyUseCase>(),
            getLeadershipUseCase = get<GetLeadershipUseCase>(),
            getMarketingUseCase = get<GetMarketingUseCase>(),
            getSalesUseCase = get<GetSalesUseCase>(),
            getOperationsUseCase = get<GetOperationsUseCase>(),
            getPeopleUseCase = get<GetPeopleUseCase>(),
            getMoneyUseCase = get<GetMoneyUseCase>(),
            getObjectiveUseCase = get<GetObjectiveUseCase>(),
            getRDUseCase = get<GetResearchAndDevelopmentUseCase>(),
            getRiskUseCase = get<GetRiskUseCase>()
        )
    }

    viewModel { StrategyViewModel(get(), get(), get()) }
    viewModel { LeadershipViewModel(get(), get(), get()) }
    viewModel { MarketingViewModel(get(), get(), get()) }
    viewModel { SalesViewModel(get(), get(), get()) }
    viewModel { OperationsViewModel(get(), get(), get()) }
    viewModel { params ->
        ObjectivesViewModel(
            getObjectiveUseCase = get(),
            updateObjectiveUseCase = get(),
            syncRepository = get(),
            initialUuid = params.getOrNull() ?: ""
        )
    }
    viewModel {
        ObjectivesListViewModel(
            getObjectiveUseCase = get(),
            deleteObjectiveUseCase = get(),
            syncRepository = get()
        )
    }
    viewModel { ResearchAndDevelopmentViewModel(get(), get(), get()) }
    viewModel { RiskViewModel(get(), get(), get()) }

    viewModel { PeopleViewModel(getPeopleUseCase = get(), updatePeopleUseCase = get(), syncRepository = get()) }

    viewModel {
        MoneyViewModel(
            getMoneyUseCase = get<GetMoneyUseCase>(),
            updateMoneyUseCase = get<UpdateMoneyUseCase>(),
            syncRepository = get()
        )
    }

    // Links — pull only, no UpdateLinksUseCase needed
    viewModel {
        LinksViewModel(
            getLinksUseCase = get<GetLinksUseCase>(),
            syncRepository = get()
        )
    }
}