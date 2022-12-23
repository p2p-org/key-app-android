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
import org.p2p.wallet.sell.ui.lock.SellTransactionDetails
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
            buttonSend.setOnClickListener {
                presenter.cashOut()
            }
            editTextTokenAmount.setOnTokenAmountChangeListener {
                presenter.onTokenAmountChanged(it)
            }
            editTextFiatAmount.setOnCurrencyAmountChangeListener {
                presenter.onCurrencyAmountChanged(it)
            }
            textViewAvailableAmount.setOnClickListener {
                presenter.onUserMaxClicked()
            }
        }
    }

    override fun showLoading(isVisible: Boolean) {
        binding.shimmerView.isVisible = isVisible
    }

    override fun showAvailableSolToSell(totalAmount: BigDecimal) {
        binding.textViewAvailableAmount.text = totalAmount.toString()
    }

    override fun navigateToSellLock(details: SellTransactionDetails) {
        replaceFragment(SellLockedFragment.create(details), addToBackStack = false)
    }

    override fun showErrorScreen() {
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
        editTextFiatAmount.setCurrencyAmount(newState.quoteAmount)
        editTextFiatAmount.setHint(getString(R.string.sell_input_fiat_symbol, newState.fiatSymbol))
        textViewFee.text = getString(R.string.sell_included_fee, newState.fee)
        textViewRate.text = getString(R.string.sell_sol_fiat_value, newState.fiat)
        editTextTokenAmount.setHint(newState.tokenSymbol)
        editTextTokenAmount.setTokenAmount(newState.solToSell)
        textViewAvailableAmount.text = getString(R.string.sell_all_sol, newState.userBalance)
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
        binding.editTextTokenAmount.setTokenAmount(newValue)
    }

    override fun setFiatAndFeeValue(newValue: String) {
        binding.editTextFiatAmount.setCurrencyAmount(newValue)
        binding.textViewFee.text = getString(R.string.sell_included_fee, newValue)
    }

    override fun setTokenAndFeeValue(newValue: String) {
        binding.editTextTokenAmount.setTokenAmount(newValue)
        binding.textViewFee.text = getString(R.string.sell_included_fee, newValue)
    }
}
