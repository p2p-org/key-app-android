package org.p2p.wallet.sell.ui.payload

import android.os.Bundle
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSellPayloadBinding
import org.p2p.wallet.sell.ui.error.SellErrorFragment
import org.p2p.wallet.sell.ui.lock.SellLockedFragment
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import java.math.BigDecimal

class SellPayloadFragment :
    BaseMvpFragment<SellPayloadContract.View, SellPayloadContract.Presenter>(
        R.layout.fragment_sell_payload
    ),
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
            presenter.load()
        }
    }

    override fun showLoading(isVisible: Boolean) {
        binding.shimmerView.isVisible = isVisible
    }

    override fun navigateToSellLock() {
        replaceFragment(SellLockedFragment.create())
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

    override fun updateViewState(newState: ViewState) = with(binding) {
        editTextFiatAmount.setText(newState.quoteAmount)
        editTextFiatAmount.setHint(newState.fiatSymbol)
        textViewFee.text = getString(R.string.sell_included_fee, newState.fee)
        textViewRate.text = getString(R.string.sell_sol_fiat_value, newState.fiat)
        binding.editTextTokenAmount.setHint(newState.tokenSymbol)
        binding.editTextTokenAmount.setText(newState.minSolToSell)
        binding.textViewAvailableAmount.text = getString(R.string.sell_all_sol, newState.userBalance)
    }

    override fun setButtonState(state: CashOutButtonState) {
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

    override fun reset(): Unit = with(binding) {
        editTextTokenAmount.setTokenAmount(BigDecimal.ZERO.toString())
        editTextFiatAmount.setCurrencyAmount(BigDecimal.ZERO.toString())
    }
}

data class CashOutButtonState(
    val isEnabled: Boolean,
    @ColorRes val backgroundColor: Int,
    @ColorRes val textColor: Int,
    val text: String
)
data class ViewState(
    val quoteAmount: String,
    val fee: String,
    val fiat: String,
    val minSolToSell: String,
    val tokenSymbol: String,
    val fiatSymbol: String,
    val userBalance: String
)
