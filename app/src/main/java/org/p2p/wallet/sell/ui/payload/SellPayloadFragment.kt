package org.p2p.wallet.sell.ui.payload

import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import org.koin.android.ext.android.inject
import org.p2p.core.utils.formatTokenForMoonpay
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSellPayloadBinding
import org.p2p.wallet.sell.analytics.SellAnalytics
import org.p2p.wallet.sell.ui.error.SellErrorFragment
import org.p2p.wallet.sell.ui.lock.SellLockedFragment
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import timber.log.Timber
import java.math.BigDecimal

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

            textViewAvailableAmount.setOnClickListener { presenter.onUserMaxClicked() }

            editTextTokenAmount.setAmountInputTextWatcher(presenter::onTokenAmountChanged)
            editTextTokenAmount.onFieldFocusChangeListener = OnFocusChangeListener { _, isFocused ->
                if (isFocused) sellAnalytics.logSellTokenAmountFocused()
            }
            editTextTokenAmount.focusAndShowKeyboard()

            editTextFiatAmount.isEditable = false

            buttonCashOut.setOnClickListener {
                sellAnalytics.logSellSubmitClicked()
                presenter.cashOut()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
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

    override fun navigateNotEnoughTokensErrorScreen(minAmount: BigDecimal) {
        popAndReplaceFragment(
            SellErrorFragment.create(
                errorState = SellErrorFragment.SellScreenError.NotEnoughAmount(
                    formattedMinTokenAmount = minAmount.formatTokenForMoonpay()
                )
            )
        )
    }

    override fun updateViewState(newState: SellPayloadContract.ViewState) = with(binding) {
        editTextFiatAmount.setAmount(newState.formattedFiatAmount)
        editTextFiatAmount.setHint(getString(R.string.sell_payload_fiat_symbol, newState.fiatSymbol))
        textViewFee.text = getString(
            R.string.sell_payload_included_fee,
            newState.formattedSellFiatFee,
            newState.fiatSymbol
        )
        textViewRate.text = getString(
            R.string.sell_payload_fiat_value,
            newState.formattedTokenPrice,
            newState.fiatSymbol
        )
        editTextTokenAmount.setHint(newState.tokenSymbol)
        editTextTokenAmount.setAmount(newState.solToSell)
        textViewAvailableAmount.text = getString(R.string.sell_payload_all_sol, newState.formattedUserAvailableBalance)
    }

    override fun setMinSolToSell(minAmount: BigDecimal, tokenSymbol: String) {
        binding.editTextTokenAmount.setHint(tokenSymbol)
        binding.editTextTokenAmount.setupText(minAmount.toString())
    }

    override fun showMoonpayWidget(url: String) {
        sellAnalytics.logSellMoonpayOpened()
        Timber.i("Sell: Opening Moonpay Sell widget: $url")
        requireContext().showUrlInCustomTabs(url)
    }

    override fun setButtonState(state: SellPayloadContract.CashOutButtonState) {
        with(binding) {
            buttonCashOut.isEnabled = state.isEnabled
            buttonCashOut.setBackgroundColor(getColor(state.backgroundColor))
            buttonCashOut.setTextColor(getColor(state.textColor))
            buttonCashOut.text = state.text

            editTextTokenAmount.showError(isVisible = !state.isEnabled)
        }
    }

    override fun setTokenAmount(newValue: String) {
        binding.editTextTokenAmount.setAmount(newValue)
    }

    override fun resetFiatAndFee(feeSymbol: String) {
        binding.editTextFiatAmount.setAmount("0")
        binding.textViewFee.text = getString(R.string.sell_payload_included_fee, "0", feeSymbol)
    }
}
