package org.p2p.wallet.jupiter

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.jupiter.api.SwapJupiterApi
import org.p2p.wallet.jupiter.api.SwapJupiterV6Api
import org.p2p.wallet.jupiter.interactor.JupiterSwapInteractor
import org.p2p.wallet.jupiter.interactor.JupiterSwapSendTransactionDelegate
import org.p2p.wallet.jupiter.interactor.SwapTokensInteractor
import org.p2p.wallet.jupiter.repository.routes.JupiterSwapRouteValidator
import org.p2p.wallet.jupiter.repository.routes.JupiterSwapRoutesInMemoryRepository
import org.p2p.wallet.jupiter.repository.routes.JupiterSwapRoutesLocalRepository
import org.p2p.wallet.jupiter.repository.routes.JupiterSwapRoutesMapper
import org.p2p.wallet.jupiter.repository.routes.JupiterSwapRoutesRemoteRepository
import org.p2p.wallet.jupiter.repository.routes.JupiterSwapRoutesRepository
import org.p2p.wallet.jupiter.repository.routes.JupiterSwapTransactionRpcErrorMapper
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensInMemoryRepository
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensLocalRepository
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRemoteRepository
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.jupiter.repository.transaction.JupiterSwapTransactionMapper
import org.p2p.wallet.jupiter.repository.transaction.JupiterSwapTransactionRemoteRepository
import org.p2p.wallet.jupiter.repository.transaction.JupiterSwapTransactionRepository
import org.p2p.wallet.jupiter.repository.v6.JupiterSwapRoutesRemoteV6Repository
import org.p2p.wallet.jupiter.repository.v6.JupiterSwapRoutesV6Mapper
import org.p2p.wallet.jupiter.repository.v6.JupiterSwapRoutesV6Repository
import org.p2p.wallet.jupiter.statemanager.SwapCoroutineScope
import org.p2p.wallet.jupiter.statemanager.SwapProfiler
import org.p2p.wallet.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.jupiter.statemanager.SwapStateManagerHolder
import org.p2p.wallet.jupiter.statemanager.SwapStateRoutesRefresher
import org.p2p.wallet.jupiter.statemanager.SwapUserTokensChangeHandler
import org.p2p.wallet.jupiter.statemanager.handler.SwapStateHandler
import org.p2p.wallet.jupiter.statemanager.handler.SwapStateInitialLoadingHandler
import org.p2p.wallet.jupiter.statemanager.handler.SwapStateLoadingRoutesHandler
import org.p2p.wallet.jupiter.statemanager.handler.SwapStateLoadingTransactionHandler
import org.p2p.wallet.jupiter.statemanager.handler.SwapStateRoutesLoadedHandler
import org.p2p.wallet.jupiter.statemanager.handler.SwapStateSwapLoadedHandler
import org.p2p.wallet.jupiter.statemanager.handler.SwapStateTokenANotZeroHandler
import org.p2p.wallet.jupiter.statemanager.handler.SwapStateTokenAZeroHandler
import org.p2p.wallet.jupiter.statemanager.rate.SwapRateTickerManager
import org.p2p.wallet.jupiter.statemanager.token_selector.SwapInitialTokenSelector
import org.p2p.wallet.jupiter.statemanager.token_selector.SwapInitialTokensData
import org.p2p.wallet.jupiter.statemanager.validator.MinimumSolAmountValidator
import org.p2p.wallet.jupiter.statemanager.validator.SwapValidator
import org.p2p.wallet.jupiter.ui.info.SwapInfoMapper
import org.p2p.wallet.jupiter.ui.main.JupiterSwapContract
import org.p2p.wallet.jupiter.ui.main.JupiterSwapPresenter
import org.p2p.wallet.jupiter.ui.main.SwapTokenRateLoader
import org.p2p.wallet.jupiter.ui.main.mapper.SwapButtonMapper
import org.p2p.wallet.jupiter.ui.main.mapper.SwapRateTickerMapper
import org.p2p.wallet.jupiter.ui.main.mapper.SwapWidgetMapper
import org.p2p.wallet.jupiter.ui.routes.SwapSelectRoutesMapper
import org.p2p.wallet.jupiter.ui.settings.JupiterSwapSettingsContract
import org.p2p.wallet.jupiter.ui.settings.presenter.JupiterSwapFeeBuilder
import org.p2p.wallet.jupiter.ui.settings.presenter.JupiterSwapSettingsPresenter
import org.p2p.wallet.jupiter.ui.settings.presenter.SwapCommonSettingsMapper
import org.p2p.wallet.jupiter.ui.settings.presenter.SwapContentSettingsMapper
import org.p2p.wallet.jupiter.ui.settings.presenter.SwapEmptySettingsMapper
import org.p2p.wallet.jupiter.ui.settings.presenter.SwapLoadingSettingsMapper
import org.p2p.wallet.jupiter.ui.tokens.SwapTokensContract
import org.p2p.wallet.jupiter.ui.tokens.SwapTokensListMode
import org.p2p.wallet.jupiter.ui.tokens.presenter.SearchSwapTokensMapper
import org.p2p.wallet.jupiter.ui.tokens.presenter.SwapTokensAMapper
import org.p2p.wallet.jupiter.ui.tokens.presenter.SwapTokensBMapper
import org.p2p.wallet.jupiter.ui.tokens.presenter.SwapTokensCommonMapper
import org.p2p.wallet.jupiter.ui.tokens.presenter.SwapTokensPresenter

object JupiterSwapModule : InjectionModule {

    const val JUPITER_RETROFIT_QUALIFIER = "JUPITER_RETROFIT_QUALIFIER"
    const val JUPITER_RETROFIT_V6_QUALIFIER = "JUPITER_RETROFIT_V6_QUALIFIER"

    override fun create() = module {
        singleOf(::SwapCoroutineScope)
        single { get<Retrofit>(named(JUPITER_RETROFIT_QUALIFIER)).create<SwapJupiterApi>() }

        singleOf(::SwapProfiler)

        factoryOf(::JupiterSwapRoutesMapper)
        factoryOf(::JupiterSwapTransactionMapper)

        factoryOf(::JupiterSwapRouteValidator)
        factoryOf(::JupiterSwapRoutesRemoteRepository) bind JupiterSwapRoutesRepository::class
        singleOf(::JupiterSwapRoutesInMemoryRepository) bind JupiterSwapRoutesLocalRepository::class
        factoryOf(::JupiterSwapTransactionRemoteRepository) bind JupiterSwapTransactionRepository::class

        factoryOf(::JupiterSwapTokensRemoteRepository) bind JupiterSwapTokensRepository::class
        singleOf(::JupiterSwapTokensInMemoryRepository) bind JupiterSwapTokensLocalRepository::class

        factoryOf(::JupiterSwapSendTransactionDelegate)
        factoryOf(::JupiterSwapTransactionRpcErrorMapper)
        factoryOf(::JupiterSwapInteractor)
        factoryOf(::SwapUserTokensChangeHandler)
        factoryOf(::MinimumSolAmountValidator)
        factoryOf(::SwapValidator)
        factoryOf(::SwapStateRoutesRefresher)
        factoryOf(::SwapWidgetMapper)
        factoryOf(::SwapButtonMapper)
        factoryOf(::SwapRateTickerMapper)

        factory { (initialData: JupiterPresenterInitialData) ->
            val stateManager: SwapStateManager = getSwapStateManager(
                initialTokensData = SwapInitialTokensData(
                    token = initialData.initialToken,
                    tokenAMint = initialData.tokenAMint,
                    tokenBMint = initialData.tokenBMint
                ),
                stateManagerHolderKey = initialData.stateManagerHolderKey
            )
            JupiterSwapPresenter(
                swapOpenedFrom = initialData.swapOpenedFrom,
                managerHolder = get(),
                widgetMapper = get(),
                buttonMapper = get(),
                rateTickerMapper = get(),
                rateTickerManager = get(),
                stateManager = stateManager,
                dispatchers = get(),
                swapInteractor = get(),
                transactionManager = get(),
                userLocalRepository = get(),
                analytics = get(),
                historyInteractor = get(),
                initialAmountA = initialData.initialAmountA,
                resources = get(),
                tokenServiceCoordinator = get(),
                alarmErrorsLogger = get()
            )
        } bind JupiterSwapContract.Presenter::class

        initJupiterSwapStateManager()
        initJupiterSwapTokensList()
        initJupiterSwapSettings()
        initV6Api()
    }

    private fun Module.initV6Api() {
        factory { get<Retrofit>(named(JUPITER_RETROFIT_V6_QUALIFIER)).create<SwapJupiterV6Api>() }
        factoryOf(::JupiterSwapRoutesRemoteV6Repository) bind JupiterSwapRoutesV6Repository::class
        factoryOf(::JupiterSwapRoutesV6Mapper)
    }

    private fun Module.initJupiterSwapStateManager() {
        factory<SwapInitialTokenSelector> { (initialSwapData: SwapInitialTokensData) ->
            SwapInitialTokenSelectorFactory.create(this, initialSwapData)
        }
        factory { (data: SwapInitialTokensData) ->
            val selectorParams = { parametersOf(data) }
            SwapStateInitialLoadingHandler(
                dispatchers = get(),
                initialTokenSelector = get(parameters = selectorParams)
            )
        }
        factoryOf(::SwapStateLoadingRoutesHandler)
        factoryOf(::SwapStateLoadingTransactionHandler)
        factoryOf(::SwapStateRoutesLoadedHandler)
        factoryOf(::SwapStateSwapLoadedHandler)
        factoryOf(::SwapStateTokenAZeroHandler)
        factoryOf(::SwapStateTokenANotZeroHandler)

        factory<Set<SwapStateHandler>> { (initialTokensData: SwapInitialTokensData) ->
            setOf(
                get<SwapStateInitialLoadingHandler>(parameters = { parametersOf(initialTokensData) }),
                get<SwapStateLoadingRoutesHandler>(),
                get<SwapStateLoadingTransactionHandler>(),
                get<SwapStateSwapLoadedHandler>(),
                get<SwapStateTokenAZeroHandler>(),
                get<SwapStateRoutesLoadedHandler>(),
                get<SwapStateTokenANotZeroHandler>(),
            )
        }
        factoryOf(::SwapTokenRateLoader)
        singleOf(::SwapStateManagerHolder)
        single {
            SwapRateTickerManager(
                swapScope = get(),
                userLocalRepository = get(),
                tokenServiceRepository = get()
            )
        }

        factory { (initialData: SwapInitialTokensData, stateManagerHolderKey: String) ->
            val managerHolder: SwapStateManagerHolder = get()
            val handlers: Set<SwapStateHandler> = get(parameters = { parametersOf(initialData) })
            managerHolder.getOrCreate(key = stateManagerHolderKey) {
                SwapStateManager(
                    dispatchers = get(),
                    handlers = handlers,
                    selectedSwapTokenStorage = get(),
                    swapValidator = get(),
                    analytics = get(),
                    tokenServiceRepository = get(),
                    tokenServiceCoordinator = get(),
                    userTokensChangeHandler = get(),
                    swapRoutesRefreshFeatureToggle = get(),
                )
            }
        }
    }

    private fun Module.initJupiterSwapSettings() {
        factory { (stateManagerHolderKey: String) ->
            SwapTokensInteractor(
                tokenServiceCoordinator = get(),
                swapTokensRepository = get(),
                swapRoutesRepository = get(),
                jupiterSwapInteractor = get(),
                swapStateManager = getSwapStateManager(
                    initialTokensData = SwapInitialTokensData.NO_DATA,
                    stateManagerHolderKey = stateManagerHolderKey
                )
            )
        }

        factoryOf(::SwapCommonSettingsMapper)
        factoryOf(::SwapInfoMapper)
        factoryOf(::SwapSelectRoutesMapper)
        factoryOf(::SwapEmptySettingsMapper)
        factoryOf(::SwapLoadingSettingsMapper)
        factoryOf(::SwapContentSettingsMapper)
        factoryOf(::JupiterSwapFeeBuilder)

        factory { (stateManagerHolderKey: String) ->
            val managerHolder: SwapStateManagerHolder = get()
            val stateManager = managerHolder.get(stateManagerHolderKey)
            JupiterSwapSettingsPresenter(
                stateManager = stateManager,
                emptyMapper = get(),
                loadingMapper = get(),
                commonMapper = get(),
                rateTickerManager = get(),
                rateTickerMapper = get(),
                contentMapper = get(),
                swapTokensRepository = get(),
                analytics = get()
            )
        } bind JupiterSwapSettingsContract.Presenter::class
    }

    private fun Module.initJupiterSwapTokensList() {
        factoryOf(::SwapTokensCommonMapper)
        factoryOf(::SwapTokensAMapper)
        factoryOf(::SwapTokensBMapper)
        factoryOf(::SearchSwapTokensMapper)
        factory { (mode: SwapTokensListMode, stateManagerHolderKey: String) ->
            SwapTokensPresenter(
                tokenToChange = mode,
                mapperA = get(),
                mapperB = get(),
                searchResultMapper = get(),
                interactor = get(parameters = { parametersOf(stateManagerHolderKey) })
            )
        } bind SwapTokensContract.Presenter::class
    }

    private fun Scope.getSwapStateManager(
        initialTokensData: SwapInitialTokensData,
        stateManagerHolderKey: String
    ): SwapStateManager {
        val params = { parametersOf(initialTokensData, stateManagerHolderKey) }
        return get(parameters = params)
    }
}
