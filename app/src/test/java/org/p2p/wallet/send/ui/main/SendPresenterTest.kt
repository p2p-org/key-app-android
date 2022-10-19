package org.p2p.wallet.send.ui.main

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.NO_ACTION
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.SELECT_FEE_PAYER
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.renbtc.interactor.BurnBtcInteractor
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.send.interactor.SearchInteractor
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.user.model.TokenData
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.CoroutineExtension
import org.p2p.wallet.utils.SpyOnInjectMockKsExtension
import java.math.BigInteger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest

@ExperimentalCoroutinesApi
@ExtendWith(SpyOnInjectMockKsExtension::class, CoroutineExtension::class)
class SendPresenterTest {

    @MockK
    lateinit var sendInteractor: SendInteractor

    @MockK
    lateinit var addressInteractor: TransactionAddressInteractor

    @MockK
    lateinit var userInteractor: UserInteractor

    @MockK
    lateinit var searchInteractor: SearchInteractor

    @MockK
    lateinit var burnBtcInteractor: BurnBtcInteractor

    @MockK
    lateinit var settingsInteractor: SettingsInteractor

    @MockK
    lateinit var tokenKeyProvider: TokenKeyProvider

    @MockK
    lateinit var browseAnalytics: BrowseAnalytics

    @MockK
    lateinit var analyticsInteractor: ScreensAnalyticsInteractor

    @MockK
    lateinit var sendAnalytics: SendAnalytics

    @MockK
    lateinit var transactionManager: TransactionManager

    @MockK
    lateinit var resourcesProvider: ResourcesProvider

    @MockK
    lateinit var dispatchers: CoroutineDispatchers

    @MockK
    lateinit var usernameDomainFeatureToggle: UsernameDomainFeatureToggle

    @MockK
    lateinit var view: SendContract.View

    @InjectMockKs(overrideValues = true)
    lateinit var testObject: SendPresenter

    @Test
    fun `check initial loading`(testDispatcher: TestDispatcher) = runTest {
        // given
        val token: Token.Active = generateSolToken()
        every { dispatchers.ui } returns testDispatcher
        coEvery { userInteractor.getUserSolToken() } returns token
        coEvery { sendInteractor.getMinRelayRentExemption() } returns BigInteger.valueOf(890880)

        // when
        testObject.loadInitialData()

        // then
        coVerifyOrder {
            view.showFullScreenLoading(isLoading = true)
            view.showNetworkDestination(NetworkType.SOLANA)
            userInteractor.getUserSolToken()
            sendInteractor.initialize(token)
            view.showSourceToken(token) // observable delegate
            sendInteractor.getMinRelayRentExemption()
            testObject.calculateTotal(sendFee = null)
            view.showFullScreenLoading(isLoading = false)
        }
    }

    @Test
    fun `check source token update`(testDispatcher: TestDispatcher) = runTest {
        // given
        val token: Token.Active = generateSolToken()
        every { dispatchers.ui } returns testDispatcher

        // when
        testObject.setSourceToken(token)

        // then
        verify { view.showDetailsError(null) }
        verify(exactly = 0) { view.showNetworkSelectionView(isVisible = true) } // this is called for renBTC only
        verify { view.showNetworkDestination(NetworkType.SOLANA) }
        verify { view.showNetworkSelectionView(isVisible = false) }

        coVerifyOrder {
            testObject.calculateRenBtcFeeIfNeeded()
            testObject.calculateByMode(token)
            testObject.updateMaxButtonVisibility(token)
            sendAnalytics.logSendChangingToken(token.tokenSymbol)

            testObject.findValidFeePayer(token, token, FeePayerSelectionStrategy.CORRECT_AMOUNT)
        }
    }

    @Test
    fun `test fee payer validation, update fee payer, NO_ACTION strategy`(testDispatcher: TestDispatcher) = runTest {
        // given
        val splToken: Token.Active = generateSplToken()
        val feePayerToken = splToken
        val feeInSol = BigInteger.valueOf(5000L)
        val feeInPayingToken = BigInteger.valueOf(10000L)
        every { dispatchers.ui } returns testDispatcher
        every { sendInteractor.getFeePayerToken() } returns splToken
        coEvery { testObject.calculateFeeRelayerFee(splToken, feePayerToken, any()) } returns (feeInSol to feeInPayingToken)

        // when
        testObject.findValidFeePayer(splToken, feePayerToken, NO_ACTION)

        // then
        verify { view.showAccountFeeViewLoading(isLoading = true) }
        coVerify { testObject.calculateFeeRelayerFee(splToken, feePayerToken, any()) }
        coVerify { testObject.showFeeDetails(splToken, feeInSol, feeInPayingToken, feePayerToken, NO_ACTION) }
        verify { view.showAccountFeeViewLoading(isLoading = false) }
    }

    @Test
    fun `test fee payer validation, calculate FR fee, fee successfully calculated`(testDispatcher: TestDispatcher) = runTest {
        // given
        val splToken: Token.Active = generateSplToken()
        val feePayerToken = splToken
        val strategy = SELECT_FEE_PAYER
        val result = SearchResult.AddressOnly(AddressState("Some address"))
        val fee: FeeRelayerFee = mockk()
        every { dispatchers.ui } returns testDispatcher
        coEvery {
            sendInteractor.calculateFeesForFeeRelayer(
                feePayerToken = feePayerToken,
                token = splToken,
                recipient = result.addressState.address
            )
        } returns fee
        every { fee.feeInPayingToken } returns BigInteger.valueOf(25000L)
        every { fee.feeInSol } returns BigInteger.valueOf(15000L)

        // when
        testObject.calculateFeeRelayerFee(splToken, feePayerToken, result)

        // then
        coVerify {
            sendInteractor.calculateFeesForFeeRelayer(splToken, feePayerToken, result.addressState.address)
        }

        verify(exactly = 0) { testObject.calculateTotal(any()) }
        verify(exactly = 0) { view.hideAccountFeeView() }
    }

    @Test
    fun `test fee payer validation, calculate FR fee, fee calculation failure`(testDispatcher: TestDispatcher) = runTest {
        // given
        val splToken: Token.Active = generateSplToken()
        val feePayerToken = splToken
        val result = SearchResult.AddressOnly(AddressState("Some address"))
        every { dispatchers.ui } returns testDispatcher
        coEvery {
            sendInteractor.calculateFeesForFeeRelayer(
                feePayerToken = feePayerToken,
                token = splToken,
                recipient = result.addressState.address
            )
        } returns null

        // when
        testObject.calculateFeeRelayerFee(splToken, feePayerToken, result)

        // then
        coVerify {
            sendInteractor.calculateFeesForFeeRelayer(splToken, feePayerToken, result.addressState.address)
        }

        verify { testObject.calculateTotal(any()) }
        verify { view.hideAccountFeeView() }
    }

    private fun generateSolToken(): Token.Active =
        Token.createSOL(
            "some public key",
            TokenData(
                WRAPPED_SOL_MINT,
                "Solana",
                "SOL",
                iconUrl = null,
                decimals = 9,
                isWrapped = false,
                serumV3Usdc = null,
                serumV3Usdt = null
            ),
            100000L,
            null
        )

    private fun generateSplToken(): Token.Active =
        Token.createSOL(
            "some public key",
            TokenData(
                "6krMGWgeqD4CySfMr94WcfcVbf2TrMzfshAk5DcZ7mbu",
                "USDT",
                "USDT",
                iconUrl = null,
                decimals = 6,
                isWrapped = false,
                serumV3Usdc = null,
                serumV3Usdt = null
            ),
            10000000L,
            null
        )
}
