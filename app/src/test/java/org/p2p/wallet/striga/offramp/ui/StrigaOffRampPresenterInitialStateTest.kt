package org.p2p.wallet.striga.offramp.ui

import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.verify
import org.junit.ClassRule
import org.junit.Test
import java.math.BigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.striga.common.model.StrigaDataLayerError
import org.p2p.wallet.striga.common.model.toFailureResult
import org.p2p.wallet.striga.offramp.models.StrigaOffRampButtonState
import org.p2p.wallet.striga.offramp.models.StrigaOffRampTokenState
import org.p2p.wallet.striga.offramp.models.StrigaOffRampTokenType
import org.p2p.wallet.utils.StandardTestCoroutineDispatchers
import org.p2p.wallet.utils.TimberUnitTestInstance
import org.p2p.wallet.utils.mutableListQueueOf

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaOffRampPresenterInitialStateTest : StrigaOffRampPresenterBaseTest() {

    companion object {
        @ClassRule
        @JvmField
        val timber = TimberUnitTestInstance(
            isEnabled = true,
            defaultTag = "StrigaOffRamp:InitialState",
            excludeMessages = listOf("java.lang.Exception: Expected error")
        )
    }

    @Test
    fun `GIVEN striga off ramp WHEN initial state THEN check exchange rate values`() = runTest {
        val presenter = createPresenter(StandardTestCoroutineDispatchers())

        presenter.attach(view)
        advanceTimeUntilRatesHasCome()

        verify(ordering = Ordering.ORDERED) {
            // 1. skeleton
            view.setRatioState(
                withArg {
                    it is TextViewCellModel.Skeleton
                }
            )
            // 2. ratio
            view.setRatioState(formatRate(exchangeRate.sellRate).toRawTextViewCellModel())
        }

        presenter.detach()
    }

    @Test
    fun `GIVEN striga off ramp WHEN initial state THEN check button is enter the amount`() = runTest {
        val presenter = createPresenter(StandardTestCoroutineDispatchers())

        presenter.attach(view)
        advanceTimeUntilRatesHasCome()

        coVerify(ordering = Ordering.ORDERED) {
            view.setButtonState(StrigaOffRampButtonState.LoadingRates)
            view.setButtonState(StrigaOffRampButtonState.EnterAmount)
        }

        presenter.detach()
    }

    @Test
    fun `GIVE striga off ramp WHEN initial state THEN check token A widget state`() = runTest {
        val presenter = createPresenter()

        presenter.attach(view)
        advanceTimeUntilRatesHasCome()

        val widgetStates = mutableListQueueOf<SwapWidgetModel>()
        verify { view.setTokenAWidgetState(capture(widgetStates)) }

        val expectedState1 = swapWidgetMapper.mapByState(
            StrigaOffRampTokenType.TokenA,
            state = swapWidgetMapper.mapTokenA(BigDecimal.ZERO, getWalletUSDCBalance())
        )
        val expectedState2 = swapWidgetMapper.mapByState(
            StrigaOffRampTokenType.TokenA,
            state = StrigaOffRampTokenState.Loading(getWalletUSDCBalance())
        )
        val expectedState3 = swapWidgetMapper.mapByState(
            StrigaOffRampTokenType.TokenA,
            state = swapWidgetMapper.mapTokenA(BigDecimal.ZERO, getWalletUSDCBalance())
        )

        // since in tests everything called sequentially, observeUsdc called earlier than observeRates
        // so the order of calls is:
        // 1. balance USDC == getWalletUSDCBalance(); amount == 0
        // 2. balance USDC == getWalletUSDCBalance(); amount == Skeleton
        // 3. balance USDC == getWalletUSDCBalance(); amount == 0 - rates are loaded

        // Log of states:
        // SwapWidgetModel.Content(
        //    titleResId=2131953180,
        //    amount=0,
        //    availableAmount=13 254.21,
        // )
        // SwapWidgetModel.Content(
        //    titleResId=2131953180,
        //    amount=Skeleton,
        //    availableAmount=13 254.21,
        // )
        // SwapWidgetModel.Content(
        //    titleResId=2131953180,
        //    amount=0,
        //    availableAmount=13 254.21,
        // )
        verify {
            view.setTokenAWidgetState(expectedState1)
            view.setTokenAWidgetState(expectedState2)
            view.setTokenAWidgetState(expectedState3)
        }

        presenter.detach()
    }

    @Test
    fun `GIVE striga off ramp WHEN initial state THEN check token B widget state`() = runTest {
        val presenter = createPresenter()

        presenter.attach(view)
        advanceTimeUntilRatesHasCome()

        val expectedState1 = swapWidgetMapper.mapByState(
            StrigaOffRampTokenType.TokenB,
            state = swapWidgetMapper.mapTokenB(BigDecimal.ZERO)
        )
        val expectedState2 = swapWidgetMapper.mapByState(
            StrigaOffRampTokenType.TokenB,
            state = StrigaOffRampTokenState.Loading(BigDecimal.ZERO)
        )
        val expectedState3 = swapWidgetMapper.mapByState(
            StrigaOffRampTokenType.TokenB,
            state = swapWidgetMapper.mapTokenB(BigDecimal.ZERO)
        )

        verify {
            view.setTokenBWidgetState(expectedState1)
            view.setTokenBWidgetState(expectedState2)
            view.setTokenBWidgetState(expectedState3)
        }

        presenter.detach()
    }

    @Test
    fun `GIVEN striga off ramp WHEN failed to get exchange rate THEN check button is disabled`() = runTest {
        coEvery { strigaExchangeRepository.getExchangeRateForPair(any(), any()) } answers {
            StrigaDataLayerError.InternalError(
                Exception("Expected error")
            ).toFailureResult()
        }
        val presenter = createPresenter()

        presenter.attach(view)
        advanceUntilIdle()

        verify(ordering = Ordering.ORDERED) {
            view.setButtonState(StrigaOffRampButtonState.LoadingRates)
            // firstly - show error
            view.showUiKitSnackBar(messageResId = R.string.error_general_message)
            // secondly - disable button
            view.setButtonState(StrigaOffRampButtonState.ErrorGeneral)
        }

        presenter.detach()
    }
}
