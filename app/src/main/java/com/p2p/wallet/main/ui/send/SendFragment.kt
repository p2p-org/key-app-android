package com.p2p.wallet.main.ui.send

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentResultListener
import com.bumptech.glide.Glide
import com.p2p.wallet.R
import com.p2p.wallet.common.bottomsheet.ErrorBottomSheet
import com.p2p.wallet.common.bottomsheet.TextContainer
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentSendBinding
import com.p2p.wallet.main.model.CurrencyMode
import com.p2p.wallet.main.ui.select.SelectTokenFragment
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.main.ui.transaction.TransactionStatusBottomSheet
import com.p2p.wallet.qr.ui.ScanQrFragment
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.utils.addFragment
import com.p2p.wallet.utils.args
import com.p2p.wallet.utils.focusAndShowKeyboard
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.resFromTheme
import com.p2p.wallet.utils.viewbinding.viewBinding
import com.p2p.wallet.utils.withArgs
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.math.BigDecimal

class SendFragment :
    BaseMvpFragment<SendContract.View, SendContract.Presenter>(R.layout.fragment_send),
    SendContract.View {

    companion object {
        private const val EXTRA_ADDRESS = "EXTRA_ADDRESS"
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        fun create(address: String? = null) = SendFragment().withArgs(
            EXTRA_ADDRESS to address
        )

        fun create(initialToken: Token) = SendFragment().withArgs(
            EXTRA_TOKEN to initialToken
        )
    }

    override val presenter: SendContract.Presenter by inject {
        parametersOf(token)
    }

    private val binding: FragmentSendBinding by viewBinding()

    private val address: String? by args(EXTRA_ADDRESS)

    private val token: Token? by args(EXTRA_TOKEN)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            sendButton.setOnClickListener { presenter.send() }

            addressEditText.doAfterTextChanged {
                val address = it.toString()
                val isEmpty = address.isEmpty()

                scanImageView.isVisible = isEmpty
                clearImageView.isVisible = !isEmpty

                presenter.setNewTargetAddress(address)
            }

            amountEditText.doAfterTextChanged {
                presenter.setNewSourceAmount(it.toString())
            }

            clearImageView.setOnClickListener {
                addressEditText.text?.clear()
            }

            scanImageView.setOnClickListener {
                val target = ScanQrFragment.create {
                    addressEditText.text?.clear()
                    addressEditText.setText(it)
                }
                addFragment(target)
            }

            feeValueTextView.setOnClickListener {
                FeeInfoBottomSheet.show(childFragmentManager)
            }

            sourceImageView.setOnClickListener {
                presenter.loadTokensForSelection()
            }

            address?.let {
                addressEditText.text?.clear()
                addressEditText.setText(it)
            }

            amountEditText.focusAndShowKeyboard()

            availableTextView.setOnClickListener {
                presenter.loadAvailableValue()
            }

            tokenTextView.setOnClickListener {
                presenter.switchCurrency()
            }
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            SelectTokenFragment.REQUEST_KEY,
            viewLifecycleOwner,
            FragmentResultListener { _, result ->
                if (!result.containsKey(SelectTokenFragment.EXTRA_TOKEN)) return@FragmentResultListener
                val token = result.getParcelable<Token>(SelectTokenFragment.EXTRA_TOKEN)
                if (token != null) presenter.setSourceToken(token)
            }
        )

        presenter.loadInitialData()
    }

    override fun navigateToTokenSelection(tokens: List<Token>) {
        addFragment(
            target = SelectTokenFragment.create(tokens),
            enter = R.anim.slide_up,
            exit = 0,
            popExit = R.anim.slide_down,
            popEnter = 0
        )
    }

    override fun showSuccess(info: TransactionInfo) {
        TransactionStatusBottomSheet.show(
            fragment = this,
            info = info,
            onDismiss = { popBackStack() }
        )
    }

    @SuppressLint("SetTextI18n")
    override fun showSourceToken(token: Token) {
        with(binding) {
            Glide.with(sourceImageView).load(token.logoUrl).into(sourceImageView)
            sourceTextView.text = token.tokenSymbol
            tokenTextView.text = token.tokenSymbol
            singleValueTextView.text = getString(R.string.main_single_value_format, token.tokenSymbol)
            usdValueTextView.text = getString(R.string.main_usd_end_format, token.getFormattedExchangeRate())
            availableTextView.text = getString(R.string.main_send_available, token.getFormattedTotal())
            /* P2P pays for send transactions, hardcoding here temporary */
            feeValueTextView.text = "0.0000 SOL"

//            addressEditText.setText("9MeTrR3fYGHeBpoQ4FxM8u3YVe8Qwo4256CajF8TWNW2")
//            amountEditText.setText("0.00001")
        }
    }

    override fun showInputValue(value: BigDecimal) {
        val textValue = "$value"
        binding.amountEditText.setText(textValue)
        binding.amountEditText.setSelection(textValue.length)
    }

    override fun showCurrencyMode(mode: CurrencyMode) {
        binding.tokenTextView.text = mode.getSymbol(requireContext())
    }

    override fun showLoading(isLoading: Boolean) {
        with(binding) {
            sendButton.isInvisible = isLoading
            buttonProgressBar.isVisible = isLoading
        }
    }

    override fun showFullScreenLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun setAvailableTextColor(availableColor: Int) {
        binding.availableTextView.setTextColor(resFromTheme(availableColor))
    }

    override fun showAvailableValue(available: BigDecimal, symbol: String) {
        binding.availableTextView.text = getString(R.string.main_send_available, "$available $symbol")
    }

    @SuppressLint("SetTextI18n")
    override fun showTokenAroundValue(tokenValue: BigDecimal, symbol: String) {
        binding.aroundTextView.text = "≈ $tokenValue $symbol"
    }

    @SuppressLint("SetTextI18n")
    override fun showUsdAroundValue(usdValue: BigDecimal) {
        binding.aroundTextView.text = getString(R.string.main_send_around_in_usd, usdValue)
    }

    override fun showButtonEnabled(isEnabled: Boolean) {
        binding.sendButton.isEnabled = isEnabled
    }

    override fun showWrongWalletError() {
        ErrorBottomSheet.show(
            fragment = this,
            iconRes = R.drawable.ic_wallet_error,
            title = TextContainer(R.string.main_send_wrong_wallet),
            message = TextContainer(R.string.main_send_wrong_wallet_message)
        )
    }
}