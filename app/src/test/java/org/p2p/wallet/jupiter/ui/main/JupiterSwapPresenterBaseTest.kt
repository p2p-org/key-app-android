package org.p2p.wallet.jupiter.ui.main

import android.content.res.Resources
import android.util.DisplayMetrics
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.jupiter.api.extension.ExtendWith
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.common.TextContainer
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.Base64String
import org.p2p.core.token.Token
import org.p2p.core.token.TokenMetadata
import org.p2p.core.utils.DecimalFormatter
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.repository.TokenServiceRepository
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.SwapRoutesRefreshFeatureToggle
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.home.model.TokenConverter
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageMock
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.jupiter.analytics.JupiterSwapMainScreenAnalytics
import org.p2p.wallet.jupiter.interactor.JupiterSwapInteractor
import org.p2p.wallet.jupiter.interactor.JupiterSwapTokensResult
import org.p2p.wallet.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.routes.JupiterSwapRoutesRepository
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.jupiter.repository.transaction.JupiterSwapTransactionRepository
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
import org.p2p.wallet.jupiter.statemanager.token_selector.CommonSwapTokenSelector
import org.p2p.wallet.jupiter.statemanager.token_selector.PreinstallTokenASelector
import org.p2p.wallet.jupiter.statemanager.token_selector.PreinstallTokensBySymbolSelector
import org.p2p.wallet.jupiter.statemanager.token_selector.SwapInitialTokenSelector
import org.p2p.wallet.jupiter.statemanager.validator.MinimumSolAmountValidator
import org.p2p.wallet.jupiter.statemanager.validator.SwapValidator
import org.p2p.wallet.jupiter.ui.main.JupiterSwapTestHelpers.toTokenData
import org.p2p.wallet.jupiter.ui.main.mapper.SwapButtonMapper
import org.p2p.wallet.jupiter.ui.main.mapper.SwapRateTickerMapper
import org.p2p.wallet.jupiter.ui.main.mapper.SwapWidgetMapper
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.sdk.facade.RelaySdkFacade
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.user.repository.UserInMemoryRepository
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.CoroutineExtension
import org.p2p.wallet.utils.JvmDecimalFormatter
import org.p2p.wallet.utils.SpyOnInjectMockKsExtension
import org.p2p.wallet.utils.UnconfinedTestDispatchers

@ExperimentalCoroutinesApi
@ExtendWith(SpyOnInjectMockKsExtension::class, CoroutineExtension::class)
open class JupiterSwapPresenterBaseTest {

    @MockK(relaxed = true)
    lateinit var view: JupiterSwapContract.View

    @MockK(relaxed = true)
    lateinit var analytics: JupiterSwapMainScreenAnalytics

    @MockK(relaxed = true)
    lateinit var transactionManager: TransactionManager

    @MockK(relaxed = true)
    lateinit var historyInteractor: HistoryInteractor

    @MockK
    lateinit var homeLocalRepository: HomeLocalRepository

    @MockK
    lateinit var jupiterSwapRoutesRepository: JupiterSwapRoutesRepository

    @MockK
    lateinit var jupiterSwapTransactionRepository: JupiterSwapTransactionRepository

    @MockK
    lateinit var jupiterSwapTokensRepository: JupiterSwapTokensRepository

    @MockK
    lateinit var swapRoutesRefreshFeatureToggle: SwapRoutesRefreshFeatureToggle

    @MockK
    lateinit var tokenServiceRepository: TokenServiceRepository

    @MockK(relaxed = true)
    lateinit var relaySdkFacade: RelaySdkFacade

    @MockK(relaxed = true)
    lateinit var tokenServiceCoordinator: TokenServiceCoordinator

    @MockK(relaxed = true)
    lateinit var tokenKeyProvider: TokenKeyProvider

    @MockK(relaxed = true)
    lateinit var rpcSolanaRepository: RpcSolanaRepository

    @MockK
    lateinit var rpcAmountRepository: RpcAmountRepository

    @MockK
    lateinit var swapButtonMapper: SwapButtonMapper

    @MockK(relaxed = true)
    lateinit var swapProfiler: SwapProfiler

    private var swapButtonMapperBackend = SwapButtonMapper()

    protected val dispatchers = UnconfinedTestDispatchers()
    protected val jupiterSwapStorage: JupiterSwapStorageContract = JupiterSwapStorageMock()

    lateinit var swapStateRoutesRefresher: SwapStateRoutesRefresher
    lateinit var jupiterSwapInteractor: JupiterSwapInteractor
    lateinit var stateManager: SwapStateManager
    lateinit var userLocalRepository: UserLocalRepository
    lateinit var swapRateTickerManager: SwapRateTickerManager

    protected fun initJupiterSwapTransactionRepository() {
        coEvery {
            jupiterSwapTransactionRepository.createSwapTransactionForRoute(any(), any())
        } answers {
            Base64String(
                "AsBPhpRjCGc81fONY4ChQ6/fHF99+z7+6+ROO9b8Ootp4M4Xx1ysqHcV6YTslJcMpGxMArR3KsxJTNElkYxxJgo" +
                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgAIABgwEh" +
                    "DK86KdYVB1Pjs1JjKmPK6g9GAZa56V/jBPUb8rFpuCstwLxi/dXxcuGW9c029J7w30uElehY5fvAA4UG2L04anLVuPPB6T" +
                    "OcVKqHLPAPFE9f57kmh3gvZW39mSwtvL6QgBKoYKFPsO1lBtl9qAg/oM9ZxvsNzynTHAOIvL4A25GT8+GpNLBGP1lJW4V7" +
                    "csG7krYohaz49p4m+rMnBlUpqLgk5ueuBsHhJO710V4qQkJYIQom3nIH5wPWUNn7D4DBkZv5SEXMv/srbpyw5vnvIzlu8X" +
                    "3EmssQ5s6QAAAAIyXJY9OJInxuz0QKRSODYMLWhOZ2v8QhASOe9jb6fhZAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                    "AAAAAAG3fbh12Whk9nL4UbO63msHLSF7V9bN5E6jPWFfv8AqTuo0J9os8wlRqxoKq+6PVhWkOu6gMIYqcw4B1JOzFlXBHn" +
                    "VLe2/a8Xs0J2EU0o0rqWXUEOzb9ArJGULtYRDWVxwIcYdPYNY3hO9sZ4NCHaQXn9E8Y63ZzHuYSTCDZvdQAgGAAUCwFwVA" +
                    "AcGAAIBFggJAAgCAQIMAgAAAADKmjsAAAAACQECAREHBgADAQoICQALIwkBAxcYDAECBA0ODxAJGRobHAkRHRITFB4VERE" +
                    "RERERBAMBJOUXy5d6460qAAIAAAACGQIHAMqaOwAAAADAUFzJFgsAADIAAAkDAgABAQkJAwMFAQkD2DABygUAAAACkygqh" +
                    "P91xYE/615NuML9TKv/zRbpHZ28IugUlXQG7D8FkZKTlJUGrYSQjCmZYH6HUEQ8qT2HJ85F8wJdRGlPmNDYCPlgfrjSgre" +
                    "CbGgFtri5urwDAAMH"
            )
        }
    }

    @Before
    open fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(Resources::class)
        // decimal formatter uses android imports
        mockkStatic(DecimalFormatter::class)

        every { Resources.getSystem().displayMetrics } returns DisplayMetrics().apply {
            density = 2.0f
            widthPixels = 1080
            heightPixels = 1920
        }
        every { DecimalFormatter.format(any(), any()) } answers {
            JvmDecimalFormatter.format(arg(0), arg(1))
        }
    }

    @After
    open fun tearDown() {
        unmockkStatic(Resources::class)
    }

    private fun initSwapButtonMapper() {
        every { swapButtonMapper.mapEnterAmount() } answers {
            Timber.tag("SwapButtonMapper").i("SwapButtonState.EnterAmount(enter amount)")
            swapButtonMapperBackend.mapEnterAmount()
        }
        every { swapButtonMapper.mapLoading() } answers {
            Timber.tag("SwapButtonMapper").i("SwapButtonState.EnterAmount(counting)")
            swapButtonMapperBackend.mapLoading()
        }

        every { swapButtonMapper.mapRouteNotFound() } answers {
            Timber.tag("SwapButtonMapper").i("SwapButtonState.EnterAmount(route not found)")
            swapButtonMapperBackend.mapRouteNotFound()
        }

        every { swapButtonMapper.mapSameToken() } answers {
            Timber.tag("SwapButtonMapper").i("SwapButtonState.EnterAmount(same token)")
            swapButtonMapperBackend.mapSameToken()
        }

        every { swapButtonMapper.mapSmallTokenAAmount() } answers {
            Timber.tag("SwapButtonMapper").i("SwapButtonState.EnterAmount(small token amount)")
            swapButtonMapperBackend.mapSmallTokenAAmount()
        }

        every { swapButtonMapper.mapInsufficientSolBalance(any(), any()) } answers {
            Timber.tag("SwapButtonMapper").i("SwapButtonState.EnterAmount(insufficient sol balance)")
            swapButtonMapperBackend.mapInsufficientSolBalance(arg(0), arg(1))
        }

        every { swapButtonMapper.mapTokenAmountNotEnough(any()) } answers {
            Timber.tag("SwapButtonMapper").i("SwapButtonState.EnterAmount(amount not enough)")
            swapButtonMapperBackend.mapTokenAmountNotEnough(arg(0))
        }

        every { swapButtonMapper.mapReadyToSwap(any(), any()) } answers {
            Timber.tag("SwapButtonMapper").i("SwapButtonState.ReadyToSwap")
            swapButtonMapperBackend.mapReadyToSwap(arg(0), arg(1))
        }
    }

    private fun initRpcAmountRepository() {
        coEvery { rpcAmountRepository.getMinBalanceForRentExemption(any()) } returns BigInteger("890880")
    }

    private fun initSwapStateRoutesRefresher() {
        swapStateRoutesRefresher = spyk(
            SwapStateRoutesRefresher(
                tokenKeyProvider = tokenKeyProvider,
                swapRoutesRepository = jupiterSwapRoutesRepository,
                swapTransactionRepository = jupiterSwapTransactionRepository,
                minSolBalanceValidator = MinimumSolAmountValidator(
                    rpcAmountRepository = rpcAmountRepository
                ),
                swapValidator = SwapValidator(),
                swapProfiler = swapProfiler
            )
        )
    }

    private fun initUserLocalRepository(tokens: List<TokenMetadata>) {
        userLocalRepository = UserInMemoryRepository(
            tokenConverter = TokenConverter,
            tokenServiceRepository = tokenServiceRepository
        ).apply {
            setTokenData(tokens)
        }
    }

    private fun initJupiterSwapInteractor(
        swapper: (Base64String) -> JupiterSwapTokensResult
    ) {
        jupiterSwapInteractor = spyk(JupiterSwapInteractor(mockk(relaxed = true)))

        coEvery { jupiterSwapInteractor.swapTokens(any(), any()) } answers {
            swapper(arg(1))
        }
    }

    private fun initJupiterSwapTokenRepository(
        allTokens: List<JupiterSwapToken>,
        tokenRate: (JupiterSwapToken) -> TokenServicePrice?,
        tokenRates: (List<JupiterSwapToken>) -> Map<Base58String, TokenServicePrice>,
    ) {
        coEvery { jupiterSwapTokensRepository.getTokens() } returns allTokens

        coEvery { jupiterSwapTokensRepository.getTokenRate(any()) } answers {
            val token: JupiterSwapToken = arg(0)
            tokenRate(token)
        }
        coEvery { tokenServiceRepository.loadPriceForTokens(any(), any()) } answers {
            val tokens: List<JupiterSwapToken> = arg(0)
            tokenRates(tokens)
        }
    }

    private fun createInitialTokenSelector(
        tokenASymbol: String? = null,
        tokenBSymbol: String? = null,
        preinstallTokenA: Token.Active? = null
    ): SwapInitialTokenSelector {
        return when {
            !tokenASymbol.isNullOrBlank() && !tokenBSymbol.isNullOrBlank() -> {
                PreinstallTokensBySymbolSelector(
                    jupiterTokensRepository = jupiterSwapTokensRepository,
                    dispatchers = dispatchers,
                    homeLocalRepository = homeLocalRepository,
                    savedSelectedSwapTokenStorage = jupiterSwapStorage,
                    preinstallTokenASymbol = tokenASymbol,
                    preinstallTokenBSymbol = tokenBSymbol,
                )
            }

            preinstallTokenA != null -> {
                PreinstallTokenASelector(
                    jupiterTokensRepository = jupiterSwapTokensRepository,
                    dispatchers = dispatchers,
                    homeLocalRepository = homeLocalRepository,
                    savedSelectedSwapTokenStorage = jupiterSwapStorage,
                    preinstallTokenA = preinstallTokenA,
                )
            }

            else -> {
                CommonSwapTokenSelector(
                    jupiterTokensRepository = jupiterSwapTokensRepository,
                    homeLocalRepository = homeLocalRepository,
                    dispatchers = dispatchers,
                    selectedSwapTokenStorage = jupiterSwapStorage
                )
            }
        }
    }

    private fun createStateManagerHandlers(
        tokenASymbol: String?,
        tokenBSymbol: String?,
        preinstallTokenA: Token.Active?
    ): Set<SwapStateHandler> {
        return setOf(
            SwapStateInitialLoadingHandler(
                dispatchers = dispatchers,
                initialTokenSelector = createInitialTokenSelector(
                    tokenASymbol, tokenBSymbol, preinstallTokenA
                )
            ),
            SwapStateLoadingRoutesHandler(
                routesRefresher = swapStateRoutesRefresher,
                selectedTokensStorage = jupiterSwapStorage,
                analytics = analytics,
            ),
            SwapStateLoadingTransactionHandler(
                routesRefresher = swapStateRoutesRefresher,
                analytics = analytics,
            ),
            SwapStateSwapLoadedHandler(
                routesRefresher = swapStateRoutesRefresher,
                analytics = analytics,
            ),
            SwapStateTokenAZeroHandler(
                swapRoutesRefresher = swapStateRoutesRefresher,
                swapValidator = SwapValidator(),
                analytics = analytics,
            ),
            SwapStateRoutesLoadedHandler(
                routesRefresher = swapStateRoutesRefresher,
                analytics = analytics,
            ),
            SwapStateTokenANotZeroHandler(
                swapRoutesRefresher = swapStateRoutesRefresher,
                selectedTokensStorage = jupiterSwapStorage,
                analytics = analytics,
            )
        )
    }

    private fun initSwapRoutesRepository(
        routesGetter: (JupiterSwapPair, Base58String) -> List<JupiterSwapRoute>,
        swappableTokenMintsGetter: (Base58String) -> List<Base58String>
    ) {
        coEvery {
            jupiterSwapRoutesRepository.getSwapRoutesForSwapPair(any(), any())
        } answers {
            routesGetter(arg(0), arg(1))
        }
        coEvery {
            jupiterSwapRoutesRepository.getSwappableTokenMints(any())
        } answers {
            swappableTokenMintsGetter(arg(0))
        }
    }

    private fun initSwapRateTickerManager() {
        swapRateTickerManager = spyk(
            SwapRateTickerManager(
                swapScope = SwapCoroutineScope(dispatchers),
                userLocalRepository = userLocalRepository,
                tokenServiceRepository = tokenServiceRepository,
                initDispatcher = dispatchers.io
            )
        )
    }

    private fun initHomeLocalRepository(
        allTokens: List<Token.Active>,
        userTokens: List<Token.Active>
    ) {
        every {
            homeLocalRepository.getTokensFlow()
        } answers {
            MutableStateFlow(allTokens)
        }
        coEvery {
            homeLocalRepository.getUserTokens()
        } answers {
            userTokens
        }
    }

    private fun initSwapStateManager(
        tokenASymbol: String?,
        tokenBSymbol: String?,
        preinstallTokenA: Token.Active?
    ) {
        stateManager = SwapStateManager(
            handlers = createStateManagerHandlers(
                tokenASymbol, tokenBSymbol, preinstallTokenA
            ),
            dispatchers = dispatchers,
            selectedSwapTokenStorage = jupiterSwapStorage,
            tokenServiceRepository = tokenServiceRepository,
            swapValidator = SwapValidator(),
            analytics = analytics,
            homeLocalRepository = homeLocalRepository,
            userTokensChangeHandler = SwapUserTokensChangeHandler(
                jupiterSwapInteractor,
                jupiterSwapTokensRepository
            ),
            swapRoutesRefreshFeatureToggle = swapRoutesRefreshFeatureToggle
        )
    }

    protected fun createPresenter(builder: JupiterTestPresenterBuilder.() -> Unit): JupiterSwapContract.Presenter {
        val data = JupiterTestPresenterBuilder().apply(builder)

        initSwapButtonMapper()
        initHomeLocalRepository(
            allTokens = data.homeRepoAllTokens,
            userTokens = data.homeRepoUserTokens
        )

        initUserLocalRepository(
            data.homeRepoAllTokens.map { it.toTokenData() }
        )

        initJupiterSwapTokenRepository(
            allTokens = data.jupiterSwapTokensRepoGetTokens,
            tokenRate = data.jupiterSwapTokensRepoGetTokenRate,
            tokenRates = data.jupiterSwapTokensRepoGetTokensRate
        )
        initRpcAmountRepository()
        initSwapStateRoutesRefresher()
        initJupiterSwapInteractor(
            swapper = data.jupiterSwapInteractorSwapTokens
        )
        initSwapStateManager(
            tokenASymbol = data.initialTokenASymbol,
            tokenBSymbol = data.initialTokenBSymbol,
            preinstallTokenA = data.preinstallTokenA
        )
        initSwapRateTickerManager()
        initSwapRoutesRepository(
            routesGetter = data.jupiterSwapRoutesRepoGetSwapRoutesForSwapPair,
            swappableTokenMintsGetter = data.jupiterSwapRoutesRepoGetSwappableTokenMints,
        )
        initJupiterSwapTransactionRepository()

        return JupiterSwapPresenter(
            swapOpenedFrom = data.swapOpenedFrom,
            managerHolder = SwapStateManagerHolder(),
            stateManager = stateManager,
            widgetMapper = SwapWidgetMapper(),
            buttonMapper = swapButtonMapper,
            rateTickerMapper = SwapRateTickerMapper(),
            rateTickerManager = swapRateTickerManager,
            swapInteractor = jupiterSwapInteractor,
            analytics = analytics,
            transactionManager = transactionManager,
            dispatchers = dispatchers,
            userLocalRepository = userLocalRepository,
            historyInteractor = historyInteractor,
            resources = mockk(relaxed = true),
            alarmErrorsLogger = mockk(relaxed = true),
            initialAmountA = data.initialAmountA,
            tokenServiceCoordinator = tokenServiceCoordinator
        )
    }

    protected fun createPresenterAndTokens(
        builder: JupiterTestPresenterBuilder.() -> Unit = {}
    ): Triple<Token.Active, Token.Active, JupiterSwapContract.Presenter> {
        val firstToken = JupiterSwapTestHelpers.createUSDCToken(BigDecimal("10.28"))
        val secondToken = JupiterSwapTestHelpers.createSOLToken(
            amount = BigDecimal("26.48"),
            rateToUsd = BigDecimal("22.14")
        )

        val presenter = createPresenter {
            homeRepoAllTokens = mutableListOf(firstToken, secondToken)
            homeRepoUserTokens = homeRepoAllTokens
            this.apply(builder)
        }

        return Triple(firstToken, secondToken, presenter)
    }

    protected fun checkFirstSwapWidgetModel(
        token: Token.Active,
        model: SwapWidgetModel?,
        expectedAmount: String,
        availableAmountNullable: Boolean = false
    ) {
        assertNotNull("SwapWidgetModel cannot be null", model)
        with(model as SwapWidgetModel.Content) {
            // "you pay"
            with(widgetTitle as TextViewCellModel.Raw) {
                val container = text as TextContainer.Res
                assertEquals(R.string.swap_main_you_pay, container.textRes)
            }
            // amount input
            with(amount as TextViewCellModel.Raw) {
                val container = text as TextContainer.Raw
                assertEquals(expectedAmount, container.text)
            }
            // all %s
            if (availableAmountNullable) {
                assertNull(availableAmount)
            } else {
                assertNotNull(availableAmount)
                with(availableAmount as TextViewCellModel.Raw) {
                    val container = text as TextContainer.Raw
                    assertEquals(token.getFormattedTotal(true), container.text)
                }
            }
            // balance %s
            with(balance as TextViewCellModel.Raw) {
                val container = text as TextContainer.ResParams
                assertEquals(R.string.swap_main_balance_amount, container.textRes)
                assertEquals(1, container.args.size)
                assertEquals(token.getFormattedTotal(), container.args.first() as String)
            }
            // token symbol
            with(currencyName as TextViewCellModel.Raw) {
                val textContainer = text as TextContainer.Raw
                assertEquals(token.tokenSymbol, textContainer.text)
            }
            // check token decimals
            assertEquals(token.decimals, amountMaxDecimals)
        }
    }

    protected fun checkSecondSwapWidgetModel(
        token: Token.Active,
        model: SwapWidgetModel?,
        expectedAmount: String = ""
    ) {
        assertNotNull("SwapWidgetModel cannot be null", model)
        assertTrue(model is SwapWidgetModel.Content)
        with(model as SwapWidgetModel.Content) {
            // "you receive"
            with(widgetTitle as TextViewCellModel.Raw) {
                val textContainer = text as TextContainer.Res
                assertEquals(R.string.swap_main_you_receive, textContainer.textRes)
            }
            // amount input (disabled)
            with(amount as TextViewCellModel.Raw) {
                val textContainer = text as TextContainer.Raw
                assertEquals(expectedAmount, textContainer.text)
            }
            // balance %s
            with(balance as TextViewCellModel.Raw) {
                val textContainer = text as TextContainer.ResParams
                assertEquals(R.string.swap_main_balance_amount, textContainer.textRes)
                assertEquals(1, textContainer.args.size)
                assertEquals(token.getFormattedTotal(), textContainer.args.first() as String)
            }
            // all %s - not available for second token
            assertNull(availableAmount)

            with(currencyName as TextViewCellModel.Raw) {
                val textContainer = text as TextContainer.Raw
                assertEquals(token.tokenSymbol, textContainer.text)
            }
            assertEquals(token.decimals, amountMaxDecimals)
        }
    }

    protected fun checkButtonStateIsDisabled(state: SwapButtonState?, textRes: Int) {
        assertTrue(state is SwapButtonState.Disabled)
        with(state as SwapButtonState.Disabled) {
            assertTrue("Wrong button text type: expected TextContainer.Res", text is TextContainer.Res)
            val container = text as TextContainer.Res
            assertEquals(textRes, container.textRes)
        }
    }

    protected fun checkButtonStateIsReadyToSwap(state: SwapButtonState?, firstToken: String, secondToken: String) {
        assertNotNull("Button state is null", state)
        assertTrue(state is SwapButtonState.ReadyToSwap)
        with(state as SwapButtonState.ReadyToSwap) {
            assertTrue(text is TextContainer.ResParams)
            val container = text as TextContainer.ResParams

            assertEquals(R.string.swap_main_button_ready_to_swap, container.textRes)
            assertEquals(2, container.args.size)
            assertEquals(firstToken, container.args[0])
            assertEquals(secondToken, container.args[1])
        }
    }

    protected fun checkButtonStateIsDisabledEnterAmount(state: SwapButtonState?) {
        assertNotNull("Button state is null", state)
        checkButtonStateIsDisabled(state, R.string.swap_main_button_enter_amount)
    }

    protected fun checkButtonStateIsDisabledCounting(state: SwapButtonState?) {
        checkButtonStateIsDisabled(state, R.string.swap_main_button_loading)
    }

    protected fun checkButtonStateIsNotEnoughAmount(state: SwapButtonState?, tokenSymbol: String) {
        assertNotNull("Button state is null", state)
        assertTrue(state is SwapButtonState.Disabled)
        with(state as SwapButtonState.Disabled) {
            val container = text as TextContainer.ResParams
            assertEquals(R.string.swap_main_button_not_enough_amount, container.textRes)
            assertEquals(1, container.args.size)
            assertEquals(tokenSymbol, container.args.first())
        }
    }
}
