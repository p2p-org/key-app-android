package org.p2p.wallet.main.ui.send

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentResultListener
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import org.p2p.wallet.R
import org.p2p.wallet.common.bottomsheet.ErrorBottomSheet
import org.p2p.wallet.common.bottomsheet.TextContainer
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSendBinding
import org.p2p.wallet.main.model.CurrencyMode
import org.p2p.wallet.main.model.NetworkType
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.ui.select.SelectTokenFragment
import org.p2p.wallet.main.ui.transaction.TransactionInfo
import org.p2p.wallet.main.ui.transaction.TransactionStatusBottomSheet
import org.p2p.wallet.qr.ui.ScanQrFragment
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.colorFromTheme
import org.p2p.wallet.utils.focusAndShowKeyboard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.utils.clearClipBoard
import org.p2p.wallet.utils.toast
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

            networkView.setOnClickListener {
                NetworkDestinationBottomSheet.show(childFragmentManager) { presenter.setNetworkDestination(it) }
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

            correctAddressSwitch.setOnCheckedChangeListener { _, isChecked ->
                val color = if (isChecked) R.attr.colorAccentPrimary else R.attr.colorAccentWarning
                errorTextView.setTextColor(colorFromTheme(color))
                presenter.setShouldAskConfirmation(!isChecked)
            }

            scanQrTextView.setOnClickListener {
                requireActivity().clearClipBoard()
            }

            p2pUsernameTextView.setOnClickListener {
            }
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            SelectTokenFragment.REQUEST_KEY,
            viewLifecycleOwner,
            FragmentResultListener { _, result ->
                if (!result.containsKey(SelectTokenFragment.EXTRA_TOKEN)) return@FragmentResultListener
                val token = result.getParcelable<Token.Active>(SelectTokenFragment.EXTRA_TOKEN)
                if (token != null) presenter.setSourceToken(token)
            }
        )

        presenter.loadInitialData()
        presenter.checkClipBoard(requireContext())
    }

    override fun navigateToTokenSelection(tokens: List<Token.Active>) {
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

    override fun showNetworkDestination(type: NetworkType) {
        binding.networkView.setBottomText(type.stringValue)
    }

    override fun showNetworkSelection() {
        binding.networkView.isVisible = true
        TransitionManager.beginDelayedTransition(binding.containerView)
    }

    override fun hideNetworkSelection() {
        binding.networkView.isVisible = false
        TransitionManager.beginDelayedTransition(binding.containerView)
    }

    override fun showAddressConfirmation() {
        binding.correctAddressGroup.isVisible = true
    }

    override fun hideAddressConfirmation() {
        binding.correctAddressGroup.isVisible = false
    }

    override fun showSourceToken(token: Token.Active) {
        with(binding) {
            Glide.with(sourceImageView).load(token.logoUrl).into(sourceImageView)
            sourceTextView.text = token.tokenSymbol
            tokenTextView.text = token.tokenSymbol
            availableTextView.text = token.getFormattedTotal()
            currentPriceView.setBottomText(token.getCurrentPrice())
        }
    }

    override fun showFee(fee: String?) {
        if (fee.isNullOrEmpty()) {
            binding.feeView.setBottomText(R.string.send_free_transaction)
            binding.feeView.setBottomTextColor(R.color.colorGreen)
            binding.feeView.setOnClickListener { FeeInfoBottomSheet.show(childFragmentManager) }
            binding.feeView.setDrawableEnd(R.drawable.ic_info)
        } else {
            binding.feeView.setBottomText(fee)
            binding.feeView.setBottomTextColorFromTheme(R.attr.colorMessagePrimary)
            binding.feeView.setOnClickListener(null)
            binding.feeView.setDrawableEnd(null)
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
        binding.tokenTextView.isVisible = !isLoading
    }

    override fun setAvailableTextColor(availableColor: Int) {
        binding.availableTextView.setTextColor(colorFromTheme(availableColor))
    }

    @SuppressLint("SetTextI18n")
    override fun showAvailableValue(available: BigDecimal, symbol: String) {
        binding.availableTextView.text = "$available $symbol"
    }

    override fun showButtonText(textRes: Int) {
        binding.sendButton.setText(textRes)
    }

    @SuppressLint("SetTextI18n")
    override fun showTokenAroundValue(tokenValue: BigDecimal, symbol: String) {
        binding.aroundTextView.text = "≈ $tokenValue $symbol"
    }

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

    override fun showBufferAddressValid() {
        TODO("Not yet implemented")
    }

    override fun showBufferAddressValidWithoutFunds() {
        TODO("Not yet implemented")
    }

    override fun showBufferUsernameResolvedOk() {
        TODO("Not yet implemented")
    }

    override fun showBufferUsernameResolvedNotOk() {
        TODO("Not yet implemented")
    }

    override fun showBufferOther() {
        TODO("Not yet implemented")
    }

    override fun setEnablePasteButton(data: String?) {
        binding.pasteTextView.setTextColor(colorFromTheme(R.attr.colorAccent))
        binding.pasteTextView.setOnClickListener {
            toast(data.toString())
        }
    }

    override fun navigateToQrFromCamera() {
        TODO("Not yet implemented")
    }

    override fun navigateToTypingResolve() {
        TODO("Not yet implemented")
    }
}