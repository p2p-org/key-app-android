package org.p2p.wallet.send.ui.main

import org.junit.jupiter.api.extension.ExtendWith
import org.p2p.wallet.utils.CoroutineExtension
import org.p2p.wallet.utils.SpyOnInjectMockKsExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExtendWith(SpyOnInjectMockKsExtension::class, CoroutineExtension::class)
class SendPresenterTest {

//    @MockK
//    lateinit var sendInteractor: SendInteractor
//
//    @MockK
//    lateinit var addressInteractor: TransactionAddressInteractor
//
//    @MockK
//    lateinit var userInteractor: UserInteractor
//
//    @MockK
//    lateinit var searchInteractor: SearchInteractor
//
//    @MockK
//    lateinit var burnBtcInteractor: BurnBtcInteractor
//
//    @MockK
//    lateinit var settingsInteractor: SettingsInteractor
//
//    @MockK
//    lateinit var tokenKeyProvider: TokenKeyProvider
//
//    @MockK
//    lateinit var browseAnalytics: BrowseAnalytics
//
//    @MockK
//    lateinit var analyticsInteractor: ScreensAnalyticsInteractor
//
//    @MockK
//    lateinit var sendAnalytics: SendAnalytics
//
//    @MockK
//    lateinit var transactionManager: TransactionManager
//
//    @MockK
//    lateinit var resources: Resources
//
//    @MockK
//    lateinit var dispatchers: CoroutineDispatchers
//
//    @MockK
//    lateinit var btcAddressValidator: BtcAddressValidator
//
//    @MockK
//    lateinit var usernameDomainFeatureToggle: UsernameDomainFeatureToggle
//
//    @MockK
//    lateinit var view: SendContract.View
//
//    @InjectMockKs(overrideValues = true)
//    lateinit var testObject: SendPresenter
//
//    @Test
//    fun `check initial loading`(testDispatcher: TestDispatcher) = runTest {
//        // given
//        val token: Token.Active = generateSolToken()
//        every { dispatchers.ui } returns testDispatcher
//        coEvery { userInteractor.getUserSolToken() } returns token
//        coEvery { sendInteractor.getMinRelayRentExemption() } returns BigInteger.valueOf(890880)
//
//        // when
//        testObject.loadInitialData()
//
//        // then
//        coVerifyOrder {
//            view.showFullScreenLoading(isLoading = true)
//            userInteractor.getUserSolToken()
//            sendInteractor.initialize(token)
//            view.showSourceToken(token) // observable delegate
//            sendInteractor.getMinRelayRentExemption()
//            testObject.calculateTotal(sendFeeRelayerFee = null)
//            view.showFullScreenLoading(isLoading = false)
//        }
//    }
//
//    @Test
//    fun `check source token update`(testDispatcher: TestDispatcher) = runTest {
//        // given
//        val token: Token.Active = generateSolToken()
//        every { dispatchers.ui } returns testDispatcher
//
//        // when
//        testObject.setSourceToken(token)
//
//        // then
//        verify { view.showDetailsError(null) }
//
//        coVerifyOrder {
//            testObject.calculateByMode(token)
//            testObject.updateMaxButtonVisibility(token)
//            sendAnalytics.logSendChangingToken(token.tokenSymbol)
//
//            testObject.findValidFeePayer(token, token, FeePayerSelectionStrategy.CORRECT_AMOUNT)
//        }
//    }
//
//    @Test
//    fun `test fee payer validation, update fee payer, NO_ACTION strategy`(testDispatcher: TestDispatcher) = runTest {
//        // given
//        val splToken: Token.Active = generateSplToken()
//        val feePayerToken = splToken
//        val fee: FeeRelayerFee = mockk()
//        every { dispatchers.ui } returns testDispatcher
//        every { sendInteractor.getFeePayerToken() } returns splToken
//        coEvery { testObject.calculateFeeRelayerFee(splToken, feePayerToken, any()) } returns fee
//        coEvery { testObject.showFeeDetails(splToken, fee, feePayerToken, NO_ACTION) } just Runs
//
//        // when
//        testObject.findValidFeePayer(splToken, feePayerToken, NO_ACTION)
//
//        // then
//        verify { view.showAccountFeeViewLoading(isLoading = true) }
//        coVerify { testObject.calculateFeeRelayerFee(splToken, feePayerToken, any()) }
//        coVerify { testObject.showFeeDetails(splToken, fee, feePayerToken, NO_ACTION) }
//        verify { view.showAccountFeeViewLoading(isLoading = false) }
//    }
//
//    @Test
//    fun `test fee payer validation, calculate FR fee, fee successfully calculated`(testDispatcher: TestDispatcher) = runTest {
//        // given
//        val splToken: Token.Active = generateSplToken()
//        val feePayerToken = splToken
//        val strategy = SELECT_FEE_PAYER
//        val result = SearchResult.AddressFound(AddressState("Some address"))
//        val fee: FeeRelayerFee = mockk()
//        every { dispatchers.ui } returns testDispatcher
//        every { fee.totalInSol } returns BigInteger.TEN
//        every { fee.totalInSpl } returns BigInteger.TEN
//        coEvery {
//            sendInteractor.calculateFeesForFeeRelayer(
//                feePayerToken = feePayerToken,
//                token = splToken,
//                recipient = result.addressState.address
//            )
//        } returns fee
//
//        // when
//        testObject.calculateFeeRelayerFee(splToken, feePayerToken, result)
//
//        // then
//        coVerify {
//            sendInteractor.calculateFeesForFeeRelayer(splToken, feePayerToken, result.addressState.address)
//        }
//
//        verify(exactly = 0) { testObject.calculateTotal(any()) }
//        verify(exactly = 0) { view.hideAccountFeeView() }
//    }
//
//    @Test
//    fun `test fee payer validation, calculate FR fee, fee calculation failure`(testDispatcher: TestDispatcher) = runTest {
//        // given
//        val splToken: Token.Active = generateSplToken()
//        val feePayerToken = splToken
//        val result = SearchResult.AddressFound(AddressState("Some address"))
//        every { dispatchers.ui } returns testDispatcher
//        coEvery {
//            sendInteractor.calculateFeesForFeeRelayer(
//                feePayerToken = feePayerToken,
//                token = splToken,
//                recipient = result.addressState.address
//            )
//        } returns null
//
//        // when
//        testObject.calculateFeeRelayerFee(splToken, feePayerToken, result)
//
//        // then
//        coVerify {
//            sendInteractor.calculateFeesForFeeRelayer(splToken, feePayerToken, result.addressState.address)
//        }
//
//        verify { testObject.calculateTotal(any()) }
//        verify { view.hideAccountFeeView() }
//    }
//
//    private fun generateSolToken(): Token.Active =
//        Token.createSOL(
//            "some public key",
//            TokenData(
//                WRAPPED_SOL_MINT,
//                "Solana",
//                "SOL",
//                iconUrl = null,
//                decimals = 9,
//                isWrapped = false,
//                serumV3Usdc = null,
//                serumV3Usdt = null
//            ),
//            100000L,
//            null
//        )
//
//    private fun generateSplToken(): Token.Active =
//        Token.createSOL(
//            "some public key",
//            TokenData(
//                "6krMGWgeqD4CySfMr94WcfcVbf2TrMzfshAk5DcZ7mbu",
//                "USDT",
//                "USDT",
//                iconUrl = null,
//                decimals = 6,
//                isWrapped = false,
//                serumV3Usdc = null,
//                serumV3Usdt = null
//            ),
//            10000000L,
//            null
//        )
}
