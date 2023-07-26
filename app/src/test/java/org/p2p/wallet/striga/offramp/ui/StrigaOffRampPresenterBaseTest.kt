package org.p2p.wallet.striga.offramp.ui

import android.content.res.Resources
import android.util.DisplayMetrics
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Before
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import org.p2p.core.common.TextContainer
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.network.ConnectionManager
import org.p2p.core.token.Token
import org.p2p.core.token.TokenExtensions
import org.p2p.core.token.TokenVisibility
import org.p2p.core.utils.Constants
import org.p2p.core.utils.DecimalFormatter
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.jupiter.ui.main.mapper.SwapRateTickerMapper
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.striga.common.model.toSuccessResult
import org.p2p.wallet.striga.exchange.models.StrigaExchangeRate
import org.p2p.wallet.striga.exchange.repository.StrigaExchangeRepository
import org.p2p.wallet.striga.offramp.StrigaOffRampContract
import org.p2p.wallet.striga.offramp.interactor.StrigaOffRampInteractor
import org.p2p.wallet.striga.offramp.interactor.polling.StrigaOffRampExchangeRateNotifier
import org.p2p.wallet.striga.offramp.mappers.StrigaOffRampMapper
import org.p2p.wallet.striga.offramp.mappers.StrigaOffRampSwapWidgetMapper
import org.p2p.wallet.striga.user.interactor.StrigaSignupDataEnsurerInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.tokenservice.UserTokensState
import org.p2p.wallet.utils.JvmDecimalFormatter
import org.p2p.wallet.utils.UnconfinedTestDispatchers

abstract class StrigaOffRampPresenterBaseTest {

    companion object {
        protected val DEFAULT_BALANCE = BigDecimal("13254.21")
    }

    @MockK(relaxed = true)
    lateinit var view: StrigaOffRampContract.View

    @MockK
    lateinit var connectionManager: ConnectionManager

    @MockK
    lateinit var strigaExchangeRepository: StrigaExchangeRepository

    // strigaSignupDataEnsurerInteractor = mockk(relaxed = true),
    //            strigaUserInteractor = mockk(relaxed = true) {
    //                every { isKycApproved } returns true
    //            },
    //            strigaWalletInteractor = mockk(relaxed = true),
    @MockK(relaxed = true)
    lateinit var strigaSignupDataEnsurerInteractor: StrigaSignupDataEnsurerInteractor

    @MockK(relaxed = true)
    lateinit var strigaUserInteractor: StrigaUserInteractor

    @MockK(relaxed = true)
    lateinit var strigaWalletInteractor: StrigaWalletInteractor

    lateinit var exchangeRateNotifier: StrigaOffRampExchangeRateNotifier
    lateinit var interactor: StrigaOffRampInteractor

    @MockK
    lateinit var tokenServiceCoordinator: TokenServiceCoordinator

    val dispatchers: CoroutineDispatchers = UnconfinedTestDispatchers()

    val hasInternetState = MutableStateFlow(true)
    val strigaOffRampMapper = StrigaOffRampMapper()
    val swapWidgetMapper = StrigaOffRampSwapWidgetMapper()
    val rateTickerMapper = SwapRateTickerMapper()

    var userWallet = MutableStateFlow<UserTokensState>(UserTokensState.Loading)

    val exchangeRate = StrigaExchangeRate(
        priceUsd = BigDecimal("0.89"),
        buyRate = BigDecimal("0.9"),
        sellRate = BigDecimal("0.88"),
        timestamp = 1689587019000,
        currencyName = "Euros",
    )

    @Before
    fun setUp() {
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

        coEvery { strigaExchangeRepository.getExchangeRateForPair(any(), any()) } returns exchangeRate.toSuccessResult()
        every { tokenServiceCoordinator.observeUserTokens() } returns userWallet
        every { tokenServiceCoordinator.refresh() } answers {
            refillUsdcBalance(getWalletUSDCBalance())
        }

        every { connectionManager.connectionStatus } returns hasInternetState

        every { strigaUserInteractor.isKycApproved } returns true

        refillUsdcBalance(DEFAULT_BALANCE)
    }

    @After
    open fun tearDown() {
        unmockkStatic(Resources::class)
    }

    protected fun createPresenter(localDispatchers: CoroutineDispatchers? = null): StrigaOffRampPresenter {
        exchangeRateNotifier = StrigaOffRampExchangeRateNotifier(
            dispatchers = localDispatchers ?: dispatchers,
            strigaExchangeRepository = strigaExchangeRepository,
        )
        interactor = StrigaOffRampInteractor(
            exchangeRateNotifier = exchangeRateNotifier
        )
        return StrigaOffRampPresenter(
            dispatchers = localDispatchers ?: dispatchers,
            connectionManager = connectionManager,
            interactor = interactor,
            strigaSignupDataEnsurerInteractor = strigaSignupDataEnsurerInteractor,
            strigaUserInteractor = strigaUserInteractor,
            strigaWalletInteractor = strigaWalletInteractor,
            tokenServiceCoordinator = tokenServiceCoordinator,
            strigaOffRampMapper = strigaOffRampMapper,
            swapWidgetMapper = swapWidgetMapper,
            rateTickerMapper = rateTickerMapper,
        )
    }

    protected fun formatRate(rate: BigDecimal): String {
        return "1 ${Constants.EUR_SYMBOL} ≈ $rate ${Constants.USDC_SYMBOL}"
    }

    protected fun String.toRawTextViewCellModel(): TextViewCellModel {
        return TextViewCellModel.Raw(
            text = TextContainer(this)
        )
    }

    protected fun Int.toResTextViewCellModel(): TextViewCellModel {
        return TextViewCellModel.Raw(
            text = TextContainer(this)
        )
    }

    protected fun getWalletUSDCBalance(): BigDecimal {
        if (userWallet.value is UserTokensState.Loaded) {
            val usdc = (userWallet.value as UserTokensState.Loaded).solTokens.find { it.isUSDC }
            return usdc?.total ?: BigDecimal.ZERO
        }

        return BigDecimal.ZERO
    }

    protected fun refillUsdcBalance(availableAmount: BigDecimal) {
        val data = UserTokensState.Loaded(
            solTokens = listOf(
                Token.Active(
                    publicKey = "pub",
                    totalInUsd = null,
                    total = availableAmount,
                    visibility = TokenVisibility.SHOWN,
                    tokenSymbol = "USDC",
                    decimals = 6,
                    mintAddress = "4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU",
                    tokenName = "USD Coin",
                    iconUrl = "https://raw.githubusercontent.com/p2p-org/solana-token-list/main/assets/mainnet/4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU/logo.png",
                    isWrapped = false,
                    rate = null,
                    currency = "USD",
                    tokenExtensions = TokenExtensions(),
                    tokenServiceAddress = "publicKey"
                )
            ),
            ethTokens = emptyList()
        )
        userWallet.tryEmit(data)
    }

    protected fun SwapWidgetModel.debugString(): String {
        if (this !is SwapWidgetModel.Content) return "SwapWidgetModel.Skeleton"

        val amount = if (amount is TextViewCellModel.Skeleton) {
            "Skeleton"
        } else {
            ((amount as? TextViewCellModel.Raw)?.text as? TextContainer.Raw)?.text
        }
        return """
            SwapWidgetModel.Content(
                titleResId=${((widgetTitle as? TextViewCellModel.Raw)?.text as? TextContainer.Res)?.textRes},
                amount=$amount,
                availableAmount=${((availableAmount as? TextViewCellModel.Raw)?.text as? TextContainer.Raw)?.text},
            )
        """.trimIndent()
    }

    protected fun TestScope.advanceTimeUntilRatesHasCome(): Unit = testScheduler.advanceTimeBy(65_000)
}
