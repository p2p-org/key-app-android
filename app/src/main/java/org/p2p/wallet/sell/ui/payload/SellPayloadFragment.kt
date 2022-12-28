package org.p2p.wallet.sell.ui.payload

import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSellPayloadBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            textViewAvailableAmount.setOnClickListener { presenter.onUserMaxClicked() }

            editTextTokenAmount.setAmountInputTextWatcher(presenter::onTokenAmountChanged)
            editTextTokenAmount.focusAndShowKeyboard()

            editTextFiatAmount.isEditable = false

            buttonSend.setOnClickListener { presenter.cashOut() }
        }
    }

    override fun showLoading(isVisible: Boolean) {
        binding.shimmerView.isVisible = isVisible
    }

    override fun showAvailableSolToSell(totalAmount: BigDecimal) {
        binding.textViewAvailableAmount.text = totalAmount.toString()
    }

    override fun navigateToSellLock(details: SellTransactionViewDetails) {
        replaceFragment(SellLockedFragment.create(details), addToBackStack = false)
    }

    override fun navigateToErrorScreen() {
        popAndReplaceFragment(
            SellErrorFragment.create(
                errorState = SellErrorFragment.SellScreenError.SERVER_ERROR
            )
        )
    }

    override fun showNotEnoughMoney(minAmount: BigDecimal) {
        popAndReplaceFragment(
            SellErrorFragment.create(
                errorState = SellErrorFragment.SellScreenError.NOT_ENOUGH_AMOUNT,
                minAmount = minAmount
            )
        )
    }

    override fun updateViewState(newState: SellPayloadContract.ViewState) = with(binding) {
        editTextFiatAmount.setAmount(newState.formattedFiatAmount)
        editTextFiatAmount.setHint(getString(R.string.sell_input_fiat_symbol, newState.fiatSymbol))
        textViewFee.text = getString(R.string.sell_included_fee, newState.formattedSellFiatFee, newState.fiatSymbol)
        textViewRate.text = getString(R.string.sell_sol_fiat_value, newState.formattedTokenPrice, newState.fiatSymbol)
        editTextTokenAmount.setHint(newState.tokenSymbol)
        editTextTokenAmount.setAmount(newState.solToSell)
        textViewAvailableAmount.text = getString(R.string.sell_all_sol, newState.formattedUserAvailableBalance)
    }

    override fun setMinSolToSell(minAmount: BigDecimal, tokenSymbol: String) {
        binding.editTextTokenAmount.setHint(tokenSymbol)
        binding.editTextTokenAmount.setupText(minAmount.toString())
    }

    override fun showMoonpayWidget(url: String) {
        Timber.i("Opening Moonpay Sell widget: $url")
        requireContext().showUrlInCustomTabs(url)
    }

    override fun setButtonState(state: SellPayloadContract.CashOutButtonState) {
        with(binding) {
            buttonSend.isEnabled = state.isEnabled
            buttonSend.setBackgroundColor(getColor(state.backgroundColor))
            buttonSend.setTextColor(getColor(state.textColor))
            buttonSend.text = state.text

            editTextTokenAmount.showError(isVisible = !state.isEnabled)
        }
    }

    override fun setTokenAmount(newValue: String) {
        binding.editTextTokenAmount.setAmount(newValue)
    }

    override fun resetFiatAndFee(feeSymbol: String) {
        binding.editTextFiatAmount.setAmount("0")
        binding.textViewFee.text = getString(R.string.sell_included_fee, "0", feeSymbol)
    }

    override fun setTokenAndFeeValue(newValue: String) {
        binding.editTextTokenAmount.setAmount(newValue)
        binding.textViewFee.text = getString(R.string.sell_included_fee, newValue)
    }
}
