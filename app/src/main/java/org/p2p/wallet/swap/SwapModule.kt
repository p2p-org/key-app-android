package org.p2p.wallet.swap

import org.koin.android.ext.koin.androidContext
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
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import org.p2p.wallet.receive.ReceiveFragmentFactory
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.swap.api.OrcaApi
import org.p2p.wallet.swap.interactor.SwapInstructionsInteractor
import org.p2p.wallet.swap.interactor.SwapSerializationInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaInfoInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaInstructionsInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaNativeSwapInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaPoolInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaRouteInteractor
import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
import org.p2p.wallet.swap.interactor.serum.SerumMarketInteractor
import org.p2p.wallet.swap.interactor.serum.SerumOpenOrdersInteractor
import org.p2p.wallet.swap.interactor.serum.SerumSwapAmountInteractor
import org.p2p.wallet.swap.interactor.serum.SerumSwapInteractor
import org.p2p.wallet.swap.interactor.serum.SerumSwapMarketInteractor
import org.p2p.wallet.swap.jupiter.api.SwapJupiterApi
import org.p2p.wallet.swap.jupiter.interactor.JupiterSwapInteractor
import org.p2p.wallet.swap.jupiter.interactor.SwapTokensInteractor
import org.p2p.wallet.swap.jupiter.repository.routes.JupiterSwapRoutesInMemoryRepository
import org.p2p.wallet.swap.jupiter.repository.routes.JupiterSwapRoutesLocalRepository
import org.p2p.wallet.swap.jupiter.repository.routes.JupiterSwapRoutesMapper
import org.p2p.wallet.swap.jupiter.repository.routes.JupiterSwapRoutesRemoteRepository
import org.p2p.wallet.swap.jupiter.repository.routes.JupiterSwapRoutesRepository
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensInMemoryRepository
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensLocalRepository
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRemoteRepository
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.swap.jupiter.repository.transaction.JupiterSwapTransactionMapper
import org.p2p.wallet.swap.jupiter.repository.transaction.JupiterSwapTransactionRemoteRepository
import org.p2p.wallet.swap.jupiter.repository.transaction.JupiterSwapTransactionRepository
import org.p2p.wallet.swap.jupiter.statemanager.SwapCoroutineScope
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManagerHolder
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateRoutesRefresher
import org.p2p.wallet.swap.jupiter.statemanager.handler.SwapStateHandler
import org.p2p.wallet.swap.jupiter.statemanager.handler.SwapStateInitialLoadingHandler
import org.p2p.wallet.swap.jupiter.statemanager.handler.SwapStateLoadingRoutesHandler
import org.p2p.wallet.swap.jupiter.statemanager.handler.SwapStateLoadingTransactionHandler
import org.p2p.wallet.swap.jupiter.statemanager.handler.SwapStateSwapLoadedHandler
import org.p2p.wallet.swap.jupiter.statemanager.handler.SwapStateTokenANotZeroHandler
import org.p2p.wallet.swap.jupiter.statemanager.handler.SwapStateTokenAZeroHandler
import org.p2p.wallet.swap.jupiter.statemanager.rate.SwapRateTickerManager
import org.p2p.wallet.swap.jupiter.statemanager.token_selector.CommonSwapTokenSelector
import org.p2p.wallet.swap.jupiter.statemanager.token_selector.PreinstallTokenASelector
import org.p2p.wallet.swap.jupiter.statemanager.validator.MinimumSolAmountValidator
import org.p2p.wallet.swap.jupiter.statemanager.validator.SwapValidator
import org.p2p.wallet.swap.repository.OrcaSwapRemoteRepository
import org.p2p.wallet.swap.repository.OrcaSwapRepository
import org.p2p.wallet.swap.ui.SwapFragmentFactory
import org.p2p.wallet.swap.ui.jupiter.info.SwapInfoMapper
import org.p2p.wallet.swap.ui.jupiter.main.JupiterSwapContract
import org.p2p.wallet.swap.ui.jupiter.main.JupiterSwapPresenter
import org.p2p.wallet.swap.ui.jupiter.main.SwapTokenRateLoader
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapButtonMapper
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapRateTickerMapper
import org.p2p.wallet.swap.ui.jupiter.main.mapper.SwapWidgetMapper
import org.p2p.wallet.swap.ui.jupiter.routes.SwapSelectRoutesMapper
import org.p2p.wallet.swap.ui.jupiter.settings.JupiterSwapSettingsContract
import org.p2p.wallet.swap.ui.jupiter.settings.presenter.JupiterSwapSettingsPresenter
import org.p2p.wallet.swap.ui.jupiter.settings.presenter.SwapCommonSettingsMapper
import org.p2p.wallet.swap.ui.jupiter.settings.presenter.SwapContentSettingsMapper
import org.p2p.wallet.swap.ui.jupiter.settings.presenter.SwapEmptySettingsMapper
import org.p2p.wallet.swap.ui.jupiter.settings.presenter.SwapLoadingSettingsMapper
import org.p2p.wallet.swap.ui.jupiter.tokens.SwapTokensContract
import org.p2p.wallet.swap.ui.jupiter.tokens.SwapTokensListMode
import org.p2p.wallet.swap.ui.jupiter.tokens.presenter.SearchSwapTokensMapper
import org.p2p.wallet.swap.ui.jupiter.tokens.presenter.SwapTokensAMapper
import org.p2p.wallet.swap.ui.jupiter.tokens.presenter.SwapTokensBMapper
import org.p2p.wallet.swap.ui.jupiter.tokens.presenter.SwapTokensCommonMapper
import org.p2p.wallet.swap.ui.jupiter.tokens.presenter.SwapTokensPresenter
import org.p2p.wallet.swap.ui.orca.OrcaSwapContract
import org.p2p.wallet.swap.ui.orca.OrcaSwapPresenter
import org.p2p.wallet.swap.ui.orca.SwapOpenedFrom

object SwapModule : InjectionModule {

    const val JUPITER_RETROFIT_QUALIFIER = "JUPITER_RETROFIT_QUALIFIER"

    override fun create() = module {
        singleOf(::SwapCoroutineScope)

        single {
            val baseUrl = androidContext().getString(R.string.orcaApiBaseUrl)
            getRetrofit(
                baseUrl = baseUrl,
                tag = "Orca",
                interceptor = null
            ).create(OrcaApi::class.java)
        }

        factoryOf(::SwapFragmentFactory)
        factoryOf(::ReceiveFragmentFactory)

        single {
            SerumSwapInteractor(
                instructionsInteractor = get(),
                openOrdersInteractor = get(),
                marketInteractor = get(),
                swapMarketInteractor = get(),
                transactionInteractor = get(),
                tokenKeyProvider = get()
            )
        }

        factory { SerumMarketInteractor(get()) }
        factory { SerumOpenOrdersInteractor(get(), get()) }
        factory { SwapSerializationInteractor(get()) }
        factory { SerumSwapAmountInteractor(get()) }
        factory { SwapInstructionsInteractor(get(), get()) }
        factory { SerumSwapMarketInteractor(get()) }

        single {
            OrcaSwapInteractor(
                feeRelayerSwapInteractor = get(),
                feeRelayerAccountInteractor = get(),
                feeRelayerInteractor = get(),
                feeRelayerTopUpInteractor = get(),
                orcaInfoInteractor = get(),
                orcaNativeSwapInteractor = get(),
                environmentManager = get()
            )
        }
        single { OrcaInfoInteractor(get(), get()) }
        single { OrcaRouteInteractor(get(), get()) }
        factory { OrcaInstructionsInteractor(get()) }
        factory { OrcaPoolInteractor(get(), get(), get(), get()) }
        factory { OrcaNativeSwapInteractor(get(), get(), get(), get(), get(), get(), get(), get(), get()) }

        factory { TransactionAddressInteractor(get(), get(), get()) }

        single { OrcaSwapRemoteRepository(get(), get(), get(), get()) } bind OrcaSwapRepository::class

        factory { (token: Token.Active?) ->
            OrcaSwapPresenter(
                resources = get(),
                initialToken = token,
                appScope = get(),
                userInteractor = get(),
                swapInteractor = get(),
                orcaPoolInteractor = get(),
                settingsInteractor = get(),
                browseAnalytics = get(),
                analyticsInteractor = get(),
                swapAnalytics = get(),
                transactionBuilderInteractor = get(),
                transactionManager = get(),
            )
        } bind OrcaSwapContract.Presenter::class

        initJupiterSwap()
    }

    private fun Module.initJupiterSwap() {
        single { get<Retrofit>(named(JUPITER_RETROFIT_QUALIFIER)).create<SwapJupiterApi>() }

        factoryOf(::JupiterSwapRoutesMapper)
        factoryOf(::JupiterSwapTransactionMapper)

        factoryOf(::JupiterSwapRoutesRemoteRepository) bind JupiterSwapRoutesRepository::class
        singleOf(::JupiterSwapRoutesInMemoryRepository) bind JupiterSwapRoutesLocalRepository::class
        factoryOf(::JupiterSwapTransactionRemoteRepository) bind JupiterSwapTransactionRepository::class

        factoryOf(::JupiterSwapTokensRemoteRepository) bind JupiterSwapTokensRepository::class
        singleOf(::JupiterSwapTokensInMemoryRepository) bind JupiterSwapTokensLocalRepository::class

        factoryOf(::JupiterSwapInteractor)
        factoryOf(::MinimumSolAmountValidator)
        factoryOf(::SwapValidator)
        factoryOf(::SwapStateRoutesRefresher)
        factoryOf(::SwapWidgetMapper)
        factoryOf(::SwapButtonMapper)
        factoryOf(::SwapRateTickerMapper)

        factory { (initialToken: Token.Active?, stateManagerHolderKey: String, swapOpenedFrom: SwapOpenedFrom) ->
            val stateManager: SwapStateManager = getSwapStateManager(initialToken, stateManagerHolderKey)
            JupiterSwapPresenter(
                swapOpenedFrom = swapOpenedFrom,
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
                analytics = get()
            )
        } bind JupiterSwapContract.Presenter::class

        initJupiterSwapStateManager()
        initJupiterSwapTokensList()
    }

    @Suppress("RemoveExplicitTypeArguments")
    private fun Module.initJupiterSwapStateManager() {
        factory { (token: Token.Active?) ->
            if (token == null) {
                CommonSwapTokenSelector(
                    jupiterTokensRepository = get(),
                    homeLocalRepository = get(),
                    dispatchers = get(),
                    selectedSwapTokenStorage = get()
                )
            } else {
                PreinstallTokenASelector(
                    jupiterTokensRepository = get(),
                    dispatchers = get(),
                    homeLocalRepository = get(),
                    savedSelectedSwapTokenStorage = get(),
                    preinstallTokenA = token,
                )
            }
        }
        factory { (token: Token.Active?) ->
            SwapStateInitialLoadingHandler(
                dispatchers = get(),
                initialTokenSelector = get(parameters = { parametersOf(token) })
            )
        }
        factoryOf(::SwapStateLoadingRoutesHandler)
        factoryOf(::SwapStateLoadingTransactionHandler)
        factoryOf(::SwapStateSwapLoadedHandler)
        factoryOf(::SwapStateTokenAZeroHandler)
        factoryOf(::SwapStateTokenANotZeroHandler)

        factory<Set<SwapStateHandler>> { (initialToken: Token.Active?) ->
            setOf(
                get<SwapStateInitialLoadingHandler>(parameters = { parametersOf(initialToken) }),
                get<SwapStateLoadingRoutesHandler>(),
                get<SwapStateLoadingTransactionHandler>(),
                get<SwapStateSwapLoadedHandler>(),
                get<SwapStateTokenAZeroHandler>(),
                get<SwapStateTokenANotZeroHandler>(),
            )
        }
        factoryOf(::SwapTokenRateLoader)
        singleOf(::SwapStateManagerHolder)
        singleOf(::SwapRateTickerManager)

        factory<SwapStateManager> { (initialToken: Token.Active?, stateManagerHolderKey: String) ->
            val managerHolder: SwapStateManagerHolder = get()
            val handlers: Set<SwapStateHandler> = get(parameters = { parametersOf(initialToken) })
            managerHolder.getOrCreate(key = stateManagerHolderKey) {
                SwapStateManager(
                    dispatchers = get(),
                    handlers = handlers,
                    selectedSwapTokenStorage = get(),
                    tokenPricesRepository = get(),
                    swapValidator = get(),
                    analytics = get(),
                )
            }
        }
    }

    private fun Module.initJupiterSwapTokensList() {
        factory { (stateManagerHolderKey: String) ->
            SwapTokensInteractor(
                homeLocalRepository = get(),
                swapTokensRepository = get(),
                swapRoutesRepository = get(),
                jupiterSwapInteractor = get(),
                swapStateManager = getSwapStateManager(
                    initialToken = null,
                    stateManagerHolderKey = stateManagerHolderKey
                )
            )
        }

        factoryOf(::SwapCommonSettingsMapper)
        factoryOf(::SwapInfoMapper)
        factoryOf(::SwapSelectRoutesMapper)
        factoryOf(::SwapEmptySettingsMapper)
        factoryOf(::SwapLoadingSettingsMapper)
        factory { (stateManagerHolderKey: String) ->
            val managerHolder: SwapStateManagerHolder = get()
            val stateManager = managerHolder.get(stateManagerHolderKey)
            SwapContentSettingsMapper(
                commonMapper = get(),
                swapStateManager = stateManager,
            )
        }
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
                contentMapper = get(parameters = { parametersOf(stateManagerHolderKey) }),
                swapTokensRepository = get(),
                analytics = get()
            )
        } bind JupiterSwapSettingsContract.Presenter::class
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
        initialToken: Token.Active?,
        stateManagerHolderKey: String
    ): SwapStateManager {
        val params = { parametersOf(initialToken, stateManagerHolderKey) }
        return get(parameters = params)
    }
}
