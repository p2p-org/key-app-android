package com.p2p.wallet.swap.ui

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.bumptech.glide.Glide
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentSwapBinding
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.ui.select.SelectTokenFragment
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.main.ui.transaction.TransactionStatusBottomSheet
import com.p2p.wallet.swap.model.Slippage
import com.p2p.wallet.utils.addFragment
import com.p2p.wallet.utils.args
import com.p2p.wallet.utils.colorFromTheme
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import com.p2p.wallet.utils.withArgs
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.math.BigDecimal

class SwapFragment :
    BaseMvpFragment<SwapContract.View, SwapContract.Presenter>(R.layout.fragment_swap),
    SwapContract.View {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"

        fun create() = SwapFragment()

        fun create(token: Token) = SwapFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    override val presenter: SwapContract.Presenter by inject {
        parametersOf(token)
    }

    private val binding: FragmentSwapBinding by viewBinding()

    private val token: Token? by args(EXTRA_TOKEN)

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
            amountEditText.doAfterTextChanged {
                presenter.setSourceAmount(it.toString())
            }

            exchangeImageView.setOnClickListener { presenter.reverseTokens() }

            reverseImageView.setOnClickListener {
                presenter.loadPrice(true)
            }

            slippageView.setOnClickListener {
                presenter.loadSlippage()
            }

            swapButton.setOnClickListener { presenter.swap() }
        }

        presenter.loadInitialData()
    }

    override fun showSourceToken(token: Token) {
        with(binding) {
            Glide.with(sourceImageView).load(token.logoUrl).into(sourceImageView)
            sourceTextView.text = token.tokenSymbol
            availableTextView.text = token.getFormattedTotal()
        }
    }

    override fun showDestinationToken(token: Token?) {
        with(binding) {
            if (token != null) {
                Glide.with(destinationImageView).load(token.logoUrl).into(destinationImageView)
                destinationTextView.text = token.tokenSymbol
                destinationAvailableTextView.isVisible = true
                destinationAvailableTextView.text = token.getFormattedTotal()
            } else {
                destinationImageView.setImageResource(R.drawable.ic_wallet)
                destinationTextView.setText(R.string.main_select)
                destinationAvailableTextView.isVisible = false
                destinationAvailableTextView.text = ""
            }
        }
    }

    override fun showButtonText(textRes: Int) {
        binding.swapButton.setText(textRes)
    }

    override fun updateInputValue(available: BigDecimal) {
        binding.amountEditText.setText("$available")
    }

    @SuppressLint("SetTextI18n")
    override fun showPrice(amount: BigDecimal, exchangeToken: String, perToken: String) {
        binding.priceGroup.isVisible = true
        binding.exchangeTextView.text = "$amount $exchangeToken per $perToken"
    }

    override fun hidePrice() {
        binding.priceGroup.isVisible = false
        binding.exchangeTextView.text = ""
    }

    @SuppressLint("SetTextI18n")
    override fun showCalculations(data: CalculationsData) {
        binding.feesGroup.isVisible = true
        binding.receiveTextView.text = getString(R.string.main_swap_min_receive, data.minReceive)
        binding.destinationAmountTextView.text = data.destinationAmount

        binding.feesTextView.setOnClickListener {
            FeesBottomSheet.show(childFragmentManager, data.liquidityProviderFee, data.fee)
        }

        binding.feesImageView.setOnClickListener {
            FeesBottomSheet.show(childFragmentManager, data.liquidityProviderFee, data.fee)
        }
    }

    override fun hideCalculations() {
        binding.feesGroup.isVisible = false
        binding.receiveTextView.text = ""
        binding.destinationAmountTextView.text = ""
    }

    @SuppressLint("SetTextI18n")
    override fun showSlippage(slippage: Double) {
        binding.slippageView.setBottomText("$slippage %")
    }

    override fun setAvailableTextColor(@ColorRes availableColor: Int) {
        val colorFromTheme = colorFromTheme(availableColor)
        binding.availableTextView.setTextColor(colorFromTheme)
        binding.availableTextView.compoundDrawables.filterNotNull().forEach {
            it.colorFilter = PorterDuffColorFilter(colorFromTheme, PorterDuff.Mode.SRC_IN)
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

    override fun openSourceSelection(tokens: List<Token>) {
        addFragment(
            target = SelectTokenFragment.create(tokens) { presenter.setNewSourceToken(it) },
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
        SlippageBottomSheet.show(childFragmentManager, currentSlippage) {
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
}