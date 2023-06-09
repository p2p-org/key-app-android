package org.p2p.wallet.svl.ui.send

import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import java.math.BigInteger
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.uikit.organisms.UiKitToolbar
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSendNewBinding
import org.p2p.wallet.home.ui.new.NewSelectTokenFragment
import org.p2p.wallet.svl.model.TemporaryAccount
import org.p2p.wallet.newsend.ui.dialogs.SendFreeTransactionsDetailsBottomSheet
import org.p2p.wallet.newsend.ui.dialogs.SendFreeTransactionsDetailsBottomSheet.OpenedFrom
import org.p2p.wallet.svl.analytics.SendViaLinkAnalytics
import org.p2p.wallet.svl.ui.linkgeneration.SendLinkGenerationFragment
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.getParcelableCompat
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone

private const val ARG_INITIAL_TOKEN = "ARG_INITIAL_TOKEN"

private const val KEY_RESULT_TOKEN_TO_SEND = "KEY_RESULT_TOKEN_TO_SEND"
private const val KEY_REQUEST_SEND = "KEY_REQUEST_SEND"

class SendViaLinkFragment :
    BaseMvpFragment<SendViaLinkContract.View, SendViaLinkContract.Presenter>(R.layout.fragment_send_new),
    SendViaLinkContract.View {

    companion object {
        fun create(
            initialToken: Token.Active? = null,
        ): SendViaLinkFragment =
            SendViaLinkFragment()
                .withArgs(ARG_INITIAL_TOKEN to initialToken)
    }

    private val initialToken: Token.Active? by args(ARG_INITIAL_TOKEN)
    private val svlAnalytics: SendViaLinkAnalytics by inject()

    private val binding: FragmentSendNewBinding by viewBinding()

    override val presenter: SendViaLinkContract.Presenter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.setInitialData(initialToken)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setupToolbar()
        binding.widgetSendDetails.apply {
            tokenClickListener = presenter::onTokenClicked
            amountListener = presenter::updateInputAmount
            maxButtonClickListener = presenter::onMaxButtonClicked
            switchListener = presenter::switchCurrencyMode
            feeButtonClickListener = presenter::onFeeInfoClicked
            focusAndShowKeyboard()
        }
        binding.sliderSend.onSlideCompleteListener = { presenter.checkInternetConnection() }
        binding.sliderSend.onSlideCollapseCompleted = { presenter.generateLink() }

        binding.switchDebug.isVisible = BuildConfig.DEBUG
        binding.textViewDebug.isVisible = BuildConfig.DEBUG
        binding.textViewMessage.isVisible = true

        requireActivity().supportFragmentManager.setFragmentResultListener(
            KEY_REQUEST_SEND,
            viewLifecycleOwner
        ) { _, result -> handleSupportFragmentResult(result) }
    }

    private fun handleSupportFragmentResult(result: Bundle) {
        when {
            // will be more!
            result.containsKey(KEY_RESULT_TOKEN_TO_SEND) -> {
                val token = result.getParcelableCompat<Token.Active>(KEY_RESULT_TOKEN_TO_SEND)!!
                presenter.updateToken(token)
            }
        }
    }

    override fun showFreeTransactionsInfo() {
        svlAnalytics.logFreeTransactionsClicked()
        SendFreeTransactionsDetailsBottomSheet.show(childFragmentManager, openedFrom = OpenedFrom.SEND_VIA_LINK)
    }

    override fun navigateToLinkGeneration(
        account: TemporaryAccount,
        token: Token.Active,
        lamports: BigInteger,
        currencyModeSymbol: String
    ) {
        val isSimulationEnabled = binding.switchDebug.isChecked
        replaceFragment(
            SendLinkGenerationFragment.create(
                recipient = account,
                token = token,
                lamports = lamports,
                isSimulation = isSimulationEnabled,
                currencyModeSymbol = currencyModeSymbol
            )
        )
    }

    override fun updateInputValue(textValue: String, forced: Boolean) {
        binding.widgetSendDetails.setInput(textValue, forced)
    }

    override fun updateInputFraction(newInputFractionLength: Int) {
        binding.widgetSendDetails.updateFractionLength(newInputFractionLength)
    }

    override fun showToken(token: Token.Active) {
        binding.widgetSendDetails.setToken(token)
    }

    override fun setMaxButtonVisible(isVisible: Boolean) {
        binding.widgetSendDetails.setMaxButtonVisible(isVisible)
    }

    override fun setInputEnabled(isEnabled: Boolean) {
        binding.widgetSendDetails.setInputEnabled(isEnabled)
    }

    override fun setBottomButtonText(text: TextContainer?) {
        binding.buttonBottom withTextOrGone text?.getString(requireContext())
    }

    override fun setSliderText(text: String?) {
        if (text.isNullOrEmpty()) {
            binding.sliderSend.isVisible = !text.isNullOrEmpty()
        } else {
            binding.sliderSend.isVisible = true
            binding.sliderSend.setActionText(text)
        }
    }

    override fun disableSwitchAmounts() {
        binding.widgetSendDetails.disableFiat()
    }

    override fun disableInputs() {
        binding.widgetSendDetails.disableInputs()
    }

    override fun showApproximateAmount(approximateAmount: String) {
        binding.widgetSendDetails.setApproximateAmount(approximateAmount)
    }

    override fun setTokenContainerEnabled(isEnabled: Boolean) {
        binding.widgetSendDetails.setTokenContainerEnabled(isEnabled = isEnabled)
    }

    override fun showFeeViewLoading(isLoading: Boolean) {
        binding.widgetSendDetails.showFeeLoading(isLoading)
    }

    override fun showDelayedFeeViewLoading(isLoading: Boolean) {
        binding.widgetSendDetails.showDelayedFeeViewLoading(isLoading)
    }

    override fun showFeeVisible(isVisible: Boolean) {
        binding.widgetSendDetails.showFeeVisible(isVisible = isVisible)
    }

    override fun setFeeLabel(text: String) {
        binding.widgetSendDetails.setFeeLabel(text)
    }

    override fun showBottomFeeValue(fee: TextViewCellModel) {
        binding.widgetSendDetails.showBottomFeeValue(fee)
    }

    override fun setFeeColor(@ColorRes colorRes: Int) {
        binding.widgetSendDetails.setBottomFeeColor(colorRes)
    }

    override fun setTotalValue(text: String) {
        binding.widgetSendDetails.setTotalValue(text)
    }

    override fun setSwitchLabel(symbol: String) {
        binding.widgetSendDetails.setSwitchLabel(getString(R.string.send_switch_to_token, symbol))
    }

    override fun setMainAmountLabel(symbol: String) {
        binding.widgetSendDetails.setMainAmountLabel(symbol)
    }

    override fun setInputColor(@ColorRes colorRes: Int) {
        binding.widgetSendDetails.setInputTextColor(colorRes)
    }

    override fun showDebugInfo(text: CharSequence) {
        binding.textViewDebug.text = text
    }

    override fun showTokenSelection(tokens: List<Token.Active>, selectedToken: Token.Active?) {
        addFragment(
            target = NewSelectTokenFragment.create(
                tokensToSelectFrom = tokens,
                selectedToken = selectedToken,
                requestKey = KEY_REQUEST_SEND,
                resultKey = KEY_RESULT_TOKEN_TO_SEND
            ),
            enter = R.anim.slide_up,
            exit = 0,
            popExit = R.anim.slide_down,
            popEnter = 0
        )
    }

    override fun showSliderCompleteAnimation() {
        binding.sliderSend.showCompleteAnimation()
    }

    override fun restoreSlider() {
        binding.sliderSend.restoreSlider()
    }

    private fun UiKitToolbar.setupToolbar() {
        setTitle(R.string.send_via_link_title_main)
        setNavigationOnClickListener { popBackStack() }
    }
}
