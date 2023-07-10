package org.p2p.wallet.jupiter.ui.main

import io.mockk.every
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.ClassRule
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.jupiter.statemanager.price_impact.SwapPriceImpactView
import org.p2p.wallet.utils.CoroutineExtension
import org.p2p.wallet.utils.SpyOnInjectMockKsExtension
import org.p2p.wallet.utils.TimberUnitTestInstance
import org.p2p.wallet.utils.back
import org.p2p.wallet.utils.mutableListQueueOf

@ExperimentalCoroutinesApi
@ExtendWith(SpyOnInjectMockKsExtension::class, CoroutineExtension::class)
class JupiterSwapPresenterPriceImpactTest : JupiterSwapPresenterBaseTest() {

    companion object {
        @ClassRule
        @JvmField
        val timber = TimberUnitTestInstance(
            defaultTag = "Swap:PriceImpact",
            excludeMessages = listOf(
                "kotlinx.coroutines.JobCancellationException"
            ),
            excludeStacktraceForMessages = listOf(
                "NotValidTokenA"
            )
        )
    }

    @Test
    fun `GIVEN swap screen WHEN price impact is below 1 percent THEN check price impact is NORMAL`() = runTest {
        val impactLessThan1Percent = BigDecimal("0.000664468464646841")

        val firstToken = JupiterSwapTestHelpers.createUSDCToken(BigDecimal("150000000.28073"))
        val secondToken = JupiterSwapTestHelpers.createSOLToken(
            amount = BigDecimal("26.48"),
            rateToUsd = BigDecimal("20.74")
        )

        every { swapRoutesRefreshFeatureToggle.durationInMilliseconds } returns 10000L

        val presenter = createPresenter {
            homeRepoAllTokens = mutableListOf(firstToken, secondToken)
            homeRepoUserTokens = homeRepoAllTokens
            jupiterSwapRoutesRepoGetSwapRoutesForSwapPair = { pair, pk ->
                listOf(
                    JupiterSwapTestHelpers.createSwapRoute(
                        TestSwapRouteData(pair, pk, priceImpact = impactLessThan1Percent)
                    )
                )
            }
        }
        presenter.attach(view)
        presenter.onTokenAmountChange("100")
        advanceUntilIdle()
        val priceImpactStates = mutableListQueueOf<SwapPriceImpactView>()

        verify(exactly = 1) { view.showPriceImpact(capture(priceImpactStates)) }
        verify(exactly = 0) { view.scrollToPriceImpact() }

        assertEquals(1, priceImpactStates.size)

        assertEquals(SwapPriceImpactView.Hidden, priceImpactStates.back())

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN price impact is 1 percent THEN check view price impact is YELLOW`() = runTest {
        val impactIsOnePercent = BigDecimal("0.01")

        val firstToken = JupiterSwapTestHelpers.createUSDCToken(BigDecimal("150000000.28073"))
        val secondToken = JupiterSwapTestHelpers.createSOLToken(
            amount = BigDecimal("26.48"),
            rateToUsd = BigDecimal("20.74")
        )

        every { swapRoutesRefreshFeatureToggle.durationInMilliseconds } returns 10000L

        val presenter = createPresenter {
            homeRepoAllTokens = mutableListOf(firstToken, secondToken)
            homeRepoUserTokens = homeRepoAllTokens
            jupiterSwapRoutesRepoGetSwapRoutesForSwapPair = { pair, pk ->
                listOf(
                    JupiterSwapTestHelpers.createSwapRoute(
                        TestSwapRouteData(pair, pk, priceImpact = impactIsOnePercent)
                    )
                )
            }
        }
        presenter.attach(view)
        presenter.onTokenAmountChange("150000000")
        advanceUntilIdle()
        val priceImpactStates = mutableListQueueOf<SwapPriceImpactView>()

        verify(exactly = 1) { view.showPriceImpact(capture(priceImpactStates)) }

        assertEquals(1, priceImpactStates.size)
        verify(exactly = 1) { view.scrollToPriceImpact() }

        assertTrue(priceImpactStates.back() is SwapPriceImpactView.Yellow)

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN price impact is below and very close to 1 percent THEN check view price impact is NORMAL`() =
        runTest {
            val impactIsVeryCloseTo1Percent = BigDecimal("0.009646486554684646864")

            val firstToken = JupiterSwapTestHelpers.createUSDCToken(BigDecimal("150000000.28073"))
            val secondToken = JupiterSwapTestHelpers.createSOLToken(
                amount = BigDecimal("26.48"),
                rateToUsd = BigDecimal("20.74")
            )

            every { swapRoutesRefreshFeatureToggle.durationInMilliseconds } returns 10000L

            val presenter = createPresenter {
                homeRepoAllTokens = mutableListOf(firstToken, secondToken)
                homeRepoUserTokens = homeRepoAllTokens
                jupiterSwapRoutesRepoGetSwapRoutesForSwapPair = { pair, pk ->
                    listOf(
                        JupiterSwapTestHelpers.createSwapRoute(
                            TestSwapRouteData(pair, pk, priceImpact = impactIsVeryCloseTo1Percent)
                        )
                    )
                }
            }
            presenter.attach(view)
            presenter.onTokenAmountChange("150000000")
            advanceUntilIdle()
            val priceImpactStates = mutableListQueueOf<SwapPriceImpactView>()

            verify(exactly = 1) { view.showPriceImpact(capture(priceImpactStates)) }
            verify(exactly = 0) { view.scrollToPriceImpact() }

            assertEquals(1, priceImpactStates.size)

            assertEquals(SwapPriceImpactView.Hidden, priceImpactStates.back())

            presenter.detach()
        }

    @Test
    fun `GIVEN swap screen WHEN price impact is 100 percent THEN check view price impact is RED`() = runTest {
        val impactIs100Percents = BigDecimal("1")

        val firstToken = JupiterSwapTestHelpers.createUSDCToken(BigDecimal("150000000.28073"))
        val secondToken = JupiterSwapTestHelpers.createSOLToken(
            amount = BigDecimal("26.48"),
            rateToUsd = BigDecimal("20.74")
        )

        every { swapRoutesRefreshFeatureToggle.durationInMilliseconds } returns 10000L

        val presenter = createPresenter {
            homeRepoAllTokens = mutableListOf(firstToken, secondToken)
            homeRepoUserTokens = homeRepoAllTokens
            jupiterSwapRoutesRepoGetSwapRoutesForSwapPair = { pair, pk ->
                listOf(
                    JupiterSwapTestHelpers.createSwapRoute(
                        TestSwapRouteData(pair, pk, priceImpact = impactIs100Percents)
                    )
                )
            }
        }
        presenter.attach(view)
        presenter.onTokenAmountChange("150000000")
        advanceUntilIdle()
        val priceImpactStates = mutableListQueueOf<SwapPriceImpactView>()

        verify(exactly = 1) { view.showPriceImpact(capture(priceImpactStates)) }
        verify(exactly = 1) { view.scrollToPriceImpact() }

        assertEquals(1, priceImpactStates.size)

        assertTrue(priceImpactStates.back() is SwapPriceImpactView.Red)

        presenter.detach()
    }
}
