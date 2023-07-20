package org.p2p.wallet.striga.offramp.ui

import io.mockk.verify
import org.junit.Test
import java.math.BigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.striga.offramp.models.StrigaOffRampButtonState
import org.p2p.wallet.striga.offramp.models.StrigaOffRampTokenType
import org.p2p.wallet.utils.divideSafe

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaOffRampPresenterCalculationTest : StrigaOffRampPresenterBaseTest() {

    @Test
    fun `GIVEN striga off ramp WHEN enter usdc amount THEN check eur amount is correct`() = runTest {
        val amountA = BigDecimal("10")
        val amountB = (amountA * exchangeRate.buyRate)
        val expectedState = swapWidgetMapper.mapByState(
            StrigaOffRampTokenType.TokenB,
            swapWidgetMapper.mapTokenB(amountB)
        )

        val presenter = createPresenter()

        presenter.attach(view)
        advanceUntilIdle()
        presenter.onTokenAAmountChange(amountA.toPlainString())
        advanceUntilIdle()

        verify(exactly = 1) { view.setTokenBWidgetState(expectedState) }

        presenter.detach()
    }

    @Test
    fun `GIVEN striga off ramp WHEN enter eur amount THEN check usdc amount is correct`() = runTest {
        val amountB = BigDecimal("10")
        val amountA = (amountB.divideSafe(exchangeRate.buyRate))
        val expectedState = swapWidgetMapper.mapByState(
            StrigaOffRampTokenType.TokenA,
            swapWidgetMapper.mapTokenA(amountA, getWalletUSDCBalance())
        )

        val presenter = createPresenter()

        presenter.attach(view)
        advanceUntilIdle()
        presenter.onTokenBAmountChange(amountB.toPlainString())
        advanceUntilIdle()

        verify(exactly = 1) { view.setTokenAWidgetState(expectedState) }

        presenter.detach()
    }

    @Test
    fun `GIVEN striga off ramp WHEN clicked all amount THEN check states for A and B tokens`() = runTest {
        val amountA = BigDecimal("4000.21")
        val amountB = amountA * exchangeRate.buyRate

        // fill balance with custom one
        refillUsdcBalance(amountA)

        val expectedStateA = swapWidgetMapper.mapByState(
            StrigaOffRampTokenType.TokenA,
            swapWidgetMapper.mapTokenA(amountA, amountA)
        )
        val expectedStateB = swapWidgetMapper.mapByState(
            StrigaOffRampTokenType.TokenB,
            swapWidgetMapper.mapTokenB(amountB)
        )

        val presenter = createPresenter()

        presenter.attach(view)
        advanceUntilIdle()
        presenter.onAllAmountClick()
        advanceUntilIdle()

        verify(exactly = 1) { view.setTokenAWidgetState(expectedStateA) }
        verify(exactly = 1) { view.setTokenBWidgetState(expectedStateB) }

        presenter.detach()
    }

    @Test
    fun `GIVEN striga off ramp WHEN enter usdc amount more than max limit THEN check error`() = runTest {
        val amountA = BigDecimal("21254.11")

        refillUsdcBalance(amountA)

        val presenter = createPresenter()

        presenter.attach(view)
        presenter.onSubmit()
        presenter.onTokenAAmountChange(amountA.toPlainString())
        advanceUntilIdle()

        verify(exactly = 1) {
            view.setButtonState(StrigaOffRampButtonState.ErrorMaxLimit)
        }

        presenter.detach()
    }

    @Test
    fun `GIVEN striga off ramp WHEN enter usdc amount more than balance and less then limit THEN check error`() =
        runTest {
            val balance = BigDecimal("13125.11")

            refillUsdcBalance(balance)

            val presenter = createPresenter()

            presenter.attach(view)
            advanceUntilIdle()
            presenter.onTokenAAmountChange("14000")
            advanceUntilIdle()

            verify(exactly = 1) { view.setButtonState(StrigaOffRampButtonState.ErrorInsufficientFunds) }

            presenter.detach()
        }
}
