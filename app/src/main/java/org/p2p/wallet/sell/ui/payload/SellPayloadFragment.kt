package org.p2p.wallet.sell.ui.payload

import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSellPayloadBinding
import org.p2p.wallet.sell.analytics.SellAnalytics
import org.p2p.wallet.sell.ui.error.SellErrorFragment
import org.p2p.wallet.sell.ui.lock.SellLockedFragment
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.sell.ui.warning.SellOnlySolWarningBottomSheet
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import timber.log.Timber

class SellPayloadFragment :
    BaseMvpFragment<SellPayloadContract.View, SellPayloadContract.Presenter>(R.layout.fragment_sell_payload),
    SellPayloadContract.View {

    companion object {
        fun create() = SellPayloadFragment()
    }

    override val presenter: SellPayloadContract.Presenter by inject()
    private val binding: FragmentSellPayloadBinding by viewBinding()
    private val sellAnalytics: SellAnalytics by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            widgetSendDetails.onMaxAmountButtonClicked = presenter::onUserMaxClicked
            widgetSendDetails.onAmountChanged = presenter::onTokenAmountChanged
            widgetSendDetails.onCurrencyModeSwitchClicked = presenter::switchCurrencyMode
            widgetSendDetails.onInputFocusChanged = View.OnFocusChangeListener { _, isFocused ->
                if (isFocused) sellAnalytics.logSellTokenAmountFocused()
            }
            widgetSendDetails.focusInputAndShowKeyboard()

            buttonCashOut.setOnClickListener {
                sellAnalytics.logSellSubmitClicked()
                presenter.cashOut()
            }
        }
    }

    override fun showLoading(isVisible: Boolean) {
        binding.shimmerView.isVisible = isVisible
    }

    override fun navigateToSellLock(details: SellTransactionViewDetails) {
        replaceFragment(SellLockedFragment.create(details))
    }

    override fun navigateToErrorScreen() {
        popAndReplaceFragment(
            SellErrorFragment.create(
                errorState = SellErrorFragment.SellScreenError.ServerError()
            )
        )
    }

    override fun showOnlySolWarning() {
        binding.root.hideKeyboard()
        SellOnlySolWarningBottomSheet.show(childFragmentManager)
    }

    override fun updateViewState(newState: SellPayloadContract.ViewState) = with(binding) {
        binding.widgetSendDetails.render(newState.widgetViewState)
        setButtonState(newState.cashOutButtonState)
    }

    override fun showMoonpayWidget(url: String) {
        sellAnalytics.logSellMoonpayOpened()
        Timber.i("Sell: Opening Moonpay Sell widget: $url")
        requireContext().showUrlInCustomTabs(url)
    }

    override fun setButtonState(state: CashOutButtonState) {
        with(binding) {
            buttonCashOut.isEnabled = state.isEnabled
            buttonCashOut.setBackgroundColor(getColor(state.backgroundColor))
            buttonCashOut.setTextColor(getColor(state.textColor))
            buttonCashOut.text = state.buttonText
        }
    }
}
