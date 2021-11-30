package org.p2p.wallet.swap.ui.orca

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.SimpleTextWatcher
import org.p2p.wallet.databinding.FragmentSwapOrcaBinding
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.ui.select.SelectTokenFragment
import org.p2p.wallet.main.ui.transaction.TransactionInfo
import org.p2p.wallet.main.ui.transaction.TransactionStatusBottomSheet
import org.p2p.wallet.swap.model.PriceData
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.OrcaAmountData
import org.p2p.wallet.swap.model.orca.OrcaFeeData
import org.p2p.wallet.swap.ui.bottomsheet.SwapFeesBottomSheet
import org.p2p.wallet.swap.ui.bottomsheet.SwapSettingsBottomSheet
import org.p2p.wallet.swap.ui.bottomsheet.SwapSlippageBottomSheet
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.colorFromTheme
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.math.BigDecimal

class OrcaSwapFragment :
    BaseMvpFragment<OrcaSwapContract.View, OrcaSwapContract.Presenter>(R.layout.fragment_swap_orca),
    OrcaSwapContract.View {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"

        fun create() = OrcaSwapFragment()

        fun create(token: Token) = OrcaSwapFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    private val token: Token? by args(EXTRA_TOKEN)

    override val presenter: OrcaSwapContract.Presenter by inject {
        parametersOf(token)
    }

    private val binding: FragmentSwapOrcaBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            toolbar.setOnMenuItemClickListener { menu ->
                if (menu.itemId == R.id.slippageMenuItem) {
                    presenter.loadDataForSwapSettings()
                    return@setOnMenuItemClickListener true
                }
                return@setOnMenuItemClickListener false
            }
            sourceImageView.setOnClickListener { presenter.loadTokensForSourceSelection() }
            destinationImageView.setOnClickListener { presenter.loadTokensForDestinationSelection() }
            destinationTextView.setOnClickListener { presenter.loadTokensForDestinationSelection() }
            availableTextView.setOnClickListener { presenter.feedAvailableValue() }
            maxTextView.setOnClickListener { presenter.feedAvailableValue() }
            amountEditText.addTextChangedListener(inputTextWatcher)

            exchangeImageView.setOnClickListener { presenter.reverseTokens() }

            slippageView.setOnClickListener {
                presenter.loadSlippage()
            }

            swapButton.setOnClickListener { presenter.swap() }
        }

        presenter.loadInitialData()
    }

    override fun showSourceToken(token: Token.Active) {
        with(binding) {
            Glide.with(sourceImageView).load(token.logoUrl).into(sourceImageView)
            sourceTextView.text = token.tokenSymbol
            maxTextView.isVisible = true
            availableTextView.isVisible = true
            availableTextView.text = token.getFormattedTotal()
        }
    }

    override fun showDestinationToken(token: Token?) {
        with(binding) {
            if (token != null) {
                Glide.with(destinationImageView).load(token.logoUrl).into(destinationImageView)
                destinationTextView.text = token.tokenSymbol
                destinationAvailableTextView.isVisible = token is Token.Active
                if (token is Token.Active) destinationAvailableTextView.text = token.getFormattedTotal()
            } else {
                destinationImageView.setImageResource(R.drawable.ic_wallet)
                destinationTextView.setText(R.string.main_select)
                destinationAvailableTextView.isVisible = false
                destinationAvailableTextView.text = ""
            }
        }
    }

    override fun showButtonText(textRes: Int, value: String?) {
        if (value.isNullOrEmpty()) {
            binding.swapButton.setText(textRes)
        } else {
            val text = getString(textRes, value)
            binding.swapButton.text = text
        }
    }

    override fun setNewAmount(sourceAmount: String) {
        binding.amountEditText.setText(sourceAmount)
    }

    @SuppressLint("SetTextI18n")
    override fun showPrice(priceData: PriceData) {
        with(binding) {
            priceGroup.isVisible = true

            var isReverse = reverseImageView.tag as? Boolean ?: false
            exchangeTextView.text = priceData.getPrice(isReverse)
            reverseImageView.setOnClickListener {
                isReverse = !isReverse
                val updated = priceData.getPrice(isReverse)
                exchangeTextView.text = updated
                reverseImageView.tag = isReverse
            }
        }
    }

    override fun hidePrice() {
        binding.priceGroup.isVisible = false
        binding.exchangeTextView.text = ""
    }

    @SuppressLint("SetTextI18n")
    override fun showCalculations(data: OrcaAmountData?) {
        with(binding) {
            if (data != null) {
                receiveTextView.text = getString(R.string.main_swap_min_receive, data.minReceiveAmount)
                destinationAmountTextView.text = data.estimatedDestinationAmount
            } else {
                receiveTextView.text = ""
                destinationAmountTextView.text = ""
            }
        }
    }

    override fun showFees(data: OrcaFeeData) {
        binding.feesGroup.isVisible = true
        binding.feesTextView.setOnClickListener {
            SwapFeesBottomSheet.show(
                childFragmentManager,
                data.liquidityProviderFee,
                data.networkFee,
                data.paymentOption
            )
        }

        binding.feesImageView.setOnClickListener {
            SwapFeesBottomSheet.show(
                childFragmentManager,
                data.liquidityProviderFee,
                data.networkFee,
                data.paymentOption
            )
        }
    }

    override fun hideCalculations() {
        binding.receiveTextView.text = ""
        binding.destinationAmountTextView.text = ""
        binding.feesGroup.isVisible = false
    }

    @SuppressLint("SetTextI18n")
    override fun showSlippage(slippage: Slippage) {
        binding.slippageView.setBottomText(slippage.percentValue)
    }

    override fun showNewAmount(amount: String) {
        binding.amountEditText.setText(amount)
        binding.amountEditText.setSelection(amount.length)
    }

    override fun setAvailableTextColor(@ColorRes availableColor: Int) {
        val colorFromTheme = colorFromTheme(availableColor)
        binding.availableTextView.setTextColor(colorFromTheme)
        binding.availableTextView.compoundDrawables.filterNotNull().forEach {
            it.colorFilter = PorterDuffColorFilter(colorFromTheme, PorterDuff.Mode.SRC_IN)
        }
    }

    override fun showError(errorText: Int?) {
        if (errorText != null) {
            binding.errorTextView.isVisible = true
            binding.errorTextView.setText(errorText)
        } else {
            binding.errorTextView.isVisible = false
        }
    }

    override fun showAroundValue(aroundValue: BigDecimal) {
        binding.aroundTextView.text = getString(R.string.main_send_around_in_usd, aroundValue)
    }

    override fun showButtonEnabled(isEnabled: Boolean) {
        binding.swapButton.isEnabled = isEnabled
    }

    override fun showSwapSuccess(info: TransactionInfo) {
        TransactionStatusBottomSheet.show(
            fragment = this,
            info = info,
            onDismiss = { popBackStack() }
        )
    }

    override fun openSourceSelection(tokens: List<Token.Active>) {
        addFragment(
            target = SelectTokenFragment.create(tokens) { presenter.setNewSourceToken(it as Token.Active) },
            enter = R.anim.slide_up,
            exit = 0,
            popExit = R.anim.slide_down,
            popEnter = 0
        )
    }

    override fun openDestinationSelection(tokens: List<Token>) {
        addFragment(
            target = SelectTokenFragment.create(tokens) { presenter.setNewDestinationToken(it) },
            enter = R.anim.slide_up,
            exit = 0,
            popExit = R.anim.slide_down,
            popEnter = 0
        )
    }

    override fun openSwapSettings(currentSlippage: Slippage) {
        SwapSettingsBottomSheet.show(childFragmentManager, currentSlippage) {
            presenter.setSlippage(it)
        }
    }

    override fun openSlippageDialog(currentSlippage: Slippage) {
        SwapSlippageBottomSheet.show(childFragmentManager, currentSlippage) {
            presenter.setSlippage(it)
        }
    }

    override fun showFullScreenLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun showLoading(isLoading: Boolean) {
        binding.buttonProgressBar.isVisible = isLoading
        binding.swapButton.isVisible = !isLoading
    }

    private val inputTextWatcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(text: Editable) {
            val amount = text.toString()
            presenter.setSourceAmount(amount)
            binding.maxTextView.isVisible = amount.isBlank()
        }
    }
}