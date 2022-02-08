package org.p2p.wallet.swap.ui.orca

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.textwatcher.SimpleTextWatcher
import org.p2p.wallet.databinding.FragmentSwapOrcaBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.ui.select.SelectTokenFragment
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.transaction.ui.ProgressBottomSheet
import org.p2p.wallet.send.ui.transaction.TransactionInfo
import org.p2p.wallet.send.ui.transaction.TransactionStatusBottomSheet
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.SwapFee
import org.p2p.wallet.swap.model.orca.SwapPrice
import org.p2p.wallet.swap.model.orca.SwapTotal
import org.p2p.wallet.swap.ui.bottomsheet.SwapSettingsBottomSheet
import org.p2p.wallet.swap.ui.settings.SwapSettingsFragment
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.colorFromTheme
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.math.BigDecimal

const val KEY_REQUEST_SWAP = "KEY_REQUEST_SWAP"
private const val EXTRA_SOURCE_TOKEN = "EXTRA_SOURCE_TOKEN"
private const val EXTRA_DESTINATION_TOKEN = "EXTRA_DESTINATION_TOKEN"
private const val EXTRA_SLIPPAGE = "EXTRA_SLIPPAGE"

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

class OrcaSwapFragment :
    BaseMvpFragment<OrcaSwapContract.View, OrcaSwapContract.Presenter>(R.layout.fragment_swap_orca),
    OrcaSwapContract.View {

    companion object {

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
                if (menu.itemId == R.id.settingsMenuItem) {
                    presenter.loadDataForSwapSettings()
                    return@setOnMenuItemClickListener true
                }
                return@setOnMenuItemClickListener false
            }
            sourceImageView.setOnClickListener { presenter.loadTokensForSourceSelection() }
            destinationImageView.setOnClickListener { presenter.loadTokensForDestinationSelection() }
            destinationTextView.setOnClickListener { presenter.loadTokensForDestinationSelection() }
            availableTextView.setOnClickListener { presenter.calculateAvailableAmount() }
            maxTextView.setOnClickListener { presenter.calculateAvailableAmount() }
            amountEditText.addTextChangedListener(inputTextWatcher)
            exchangeImageView.setOnClickListener { presenter.reverseTokens() }
            swapDetails.setOnSlippageClickListener {
                presenter.loadDataForSwapSettings()
            }
            swapDetails.setOnPayFeeClickListener {
                presenter.loadTransactionTokens()
            }

            swapButton.setOnClickListener { presenter.swap() }
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SWAP,
            viewLifecycleOwner
        ) { _, result ->
            when {
                result.containsKey(EXTRA_SOURCE_TOKEN) -> {
                    val token = result.getParcelable<Token.Active>(EXTRA_SOURCE_TOKEN)
                    if (token != null) presenter.setNewSourceToken(token)
                }
                result.containsKey(EXTRA_DESTINATION_TOKEN) -> {
                    val token = result.getParcelable<Token.Active>(EXTRA_DESTINATION_TOKEN)
                    if (token != null) presenter.setNewDestinationToken(token)
                }
                result.containsKey(EXTRA_SLIPPAGE) -> {
                    val slippage = result.getParcelable<Slippage>(EXTRA_SLIPPAGE)
                    if (slippage != null) presenter.setSlippage(slippage)
                }
            }
        }

        presenter.loadInitialData()
    }

    override fun showSourceToken(token: Token.Active) {
        with(binding) {
            Glide.with(sourceImageView).load(token.iconUrl).into(sourceImageView)
            sourceTextView.text = token.tokenSymbol
            availableTextView.isVisible = true
            availableTextView.text = token.getFormattedTotal()
            swapDetails.showTotal(null)
        }
    }

    override fun showDestinationToken(token: Token?) {
        with(binding) {
            if (token != null) {
                Glide.with(destinationImageView).load(token.iconUrl).into(destinationImageView)
                destinationTextView.text = token.tokenSymbol
                destinationAvailableTextView.isVisible = token is Token.Active
                if (token is Token.Active) destinationAvailableTextView.text = token.getFormattedTotal()
            } else {
                destinationImageView.setImageResource(R.drawable.ic_question)
                destinationTextView.setText(R.string.main_select)
                destinationAvailableTextView.isVisible = false
                destinationAvailableTextView.text = ""
            }
        }
    }

    override fun openSwapSettings(tokens: List<Token.Active>, selectedToken: String) {
        addFragment(SwapSettingsFragment.create(tokens, selectedToken, EXTRA_SLIPPAGE))
    }

    override fun showButtonText(textRes: Int, iconRes: Int?, vararg value: String) {
        binding.swapButton.setStartIcon(iconRes)

        if (value.isEmpty()) {
            binding.swapButton.setActionText(textRes)
        } else {
            val text = getString(textRes, *value)
            binding.swapButton.setActionText(text)
        }
    }

    override fun setNewAmount(sourceAmount: String) {
        binding.amountEditText.setText(sourceAmount)
    }

    @SuppressLint("SetTextI18n")
    override fun showPrice(data: SwapPrice?) {
        binding.swapDetails.showPrice(data)
    }

    @SuppressLint("SetTextI18n")
    override fun showTotal(data: SwapTotal?) {
        with(binding) {
            swapDetails.showTotal(data)

            if (data != null) {
                receiveAtLeastLabelTextView.text = getString(R.string.main_swap_min_receive)
                receiveTextView.text = data.receiveAtLeast
                receiveUsdTextView.text = data.receiveAtLeastUsd
                destinationAmountTextView.text = data.destinationAmount
            } else {
                receiveAtLeastLabelTextView.text = ""
                receiveTextView.text = ""
                receiveUsdTextView.text = ""
                destinationAmountTextView.text = ""
            }
        }
    }

    override fun showFees(data: SwapFee?) {
        binding.swapDetails.showFee(data)
    }

    override fun showSlippage(slippage: Slippage) {
        binding.swapDetails.showSlippage(slippage)
    }

    override fun showNewAmount(amount: String) {
        binding.amountEditText.removeTextChangedListener(inputTextWatcher)
        binding.amountEditText.setText(amount)
        binding.amountEditText.setSelection(amount.length)
        binding.amountEditText.addTextChangedListener(inputTextWatcher)
    }

    override fun setAvailableTextColor(@ColorRes availableColor: Int) {
        val colorFromTheme = colorFromTheme(availableColor)
        binding.availableTextView.setTextColor(colorFromTheme)
        binding.availableTextView.compoundDrawables.filterNotNull().forEach {
            it.colorFilter = PorterDuffColorFilter(colorFromTheme, PorterDuff.Mode.SRC_IN)
        }
    }

    override fun showError(@StringRes errorText: Int?) {
        binding.swapDetails.showError(errorText)
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
            target = SelectTokenFragment.create(tokens, EXTRA_SOURCE_TOKEN),
            enter = R.anim.slide_up,
            exit = 0,
            popExit = R.anim.slide_down,
            popEnter = 0
        )
    }

    override fun openDestinationSelection(tokens: List<Token>) {
        addFragment(
            target = SelectTokenFragment.create(tokens, EXTRA_DESTINATION_TOKEN),
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

    override fun showFullScreenLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun showLoading(isLoading: Boolean) {
        binding.swapButton.setLoading(isLoading)
    }

    override fun showProgressDialog(data: ShowProgress?) {
        if (data != null) {
            ProgressBottomSheet.show(childFragmentManager, data)
        } else {
            ProgressBottomSheet.hide(childFragmentManager)
        }
    }

    private val inputTextWatcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(text: Editable) {
            val amount = text.toString()
            presenter.setSourceAmount(amount)
        }
    }
}