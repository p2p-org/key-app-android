package org.p2p.wallet.moonpay.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.textwatcher.PrefixSuffixTextWatcher
import org.p2p.wallet.databinding.FragmentBuySolanaBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.moonpay.model.BuyData
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone

class BuySolanaFragment :
    BaseMvpFragment<BuySolanaContract.View, BuySolanaContract.Presenter>(R.layout.fragment_buy_solana),
    BuySolanaContract.View {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        fun create(token: Token) = BuySolanaFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    override val presenter: BuySolanaContract.Presenter by inject {
        parametersOf(token)
    }
    private val token: Token by args(EXTRA_TOKEN)

    private val binding: FragmentBuySolanaBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private var backPressedCallback: OnBackPressedCallback? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backPressedCallback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            presenter.onBackPressed()
        }
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Buy.SOL)
        with(binding) {
            toolbar.title = getString(R.string.buy_token_on_moonpay, token.tokenSymbol)
            toolbar.setNavigationOnClickListener { popBackStack() }
            getValueTextView.text = getString(R.string.buy_zero_token, token.tokenSymbol)
            priceView.setLabelText(getString(R.string.buy_token_price, token.tokenSymbol))
            purchaseCostView.setLabelText(getString(R.string.buy_token_purchase_cost, token.tokenSymbol))
            accountCreationView.setLabelText(getString(R.string.buy_account_creation, token.tokenSymbol))

            installPrefixWatcher()

            continueButton.setOnClickListener {
                presenter.onContinueClicked()
            }
            getValueTextView.setOnClickListener {
                presenter.onSwapClicked()
            }
        }
        presenter.loadData()
    }

    override fun showTokenPrice(price: String) {
        binding.priceView.setValueText(price)
    }

    override fun showData(data: BuyData) {
        with(binding) {
            priceView.setValueText(data.priceText)
            getValueTextView.text = data.receiveAmountText
            processingFeeView.setValueText(data.processingFeeText)
            networkFeeView.setValueText(data.networkFeeText)
            extraFeeView.setValueText(data.extraFeeText)
            accountCreationView.setValueText(data.accountCreationCostText)
            data.purchaseCostText?.let {
                purchaseCostView.setValueText(it)
            }

            totalView.setValueText(data.totalText)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressBar.isInvisible = !isLoading
    }

    override fun showMessage(message: String?) {
        binding.apply {
            errorTextView withTextOrGone message
            continueButton.isEnabled = !hasInputError()
        }
    }

    override fun navigateToMoonpay(amount: String) {
        val sol = Constants.SOL_SYMBOL.lowercase()
        val currencyCode = if (token.isSOL) sol else "${token.tokenSymbol.lowercase()}_$sol"
        replaceFragment(MoonpayViewFragment.create(amount, currencyCode))
    }

    override fun close() {
        popBackStack()
    }

    override fun onDetach() {
        super.onDetach()
        backPressedCallback?.remove()
    }

    override fun swapData(isSwapped: Boolean, prefixSuffixSymbol: String) = with(binding) {
        if (isSwapped) {
            payTextView.setText(R.string.buy_you_get)
            getTextView.setText(R.string.buy_you_pay)
        } else {
            payTextView.setText(R.string.buy_you_pay)
            getTextView.setText(R.string.buy_you_get)
        }
        val isSuffix = prefixSuffixSymbol != Constants.USD_SYMBOL
        installPrefixWatcher(isSwapped, prefixSuffixSymbol)
        payEditText.hint = if (isSuffix) {
            "0 $prefixSuffixSymbol"
        } else {
            "${prefixSuffixSymbol}0"
        }
        payEditText.text = payEditText.text
    }

    private fun installPrefixWatcher(
        isSwapped: Boolean = false,
        prefixSuffixSymbol: String = Constants.USD_SYMBOL
    ) = with(binding) {
        val isSuffix = prefixSuffixSymbol != Constants.USD_SYMBOL
        val finalPrefixSuffixSymbol = if (isSuffix) " $prefixSuffixSymbol" else prefixSuffixSymbol
        PrefixSuffixTextWatcher.uninstallFrom(payEditText)
        PrefixSuffixTextWatcher.installOn(payEditText, finalPrefixSuffixSymbol, isSuffix = isSuffix) { data ->
            if (!isSwapped) purchaseCostView.setValueText(data.prefixText)
            continueButton.isEnabled = data.prefixText.isNotEmpty() && !hasInputError()
            presenter.setBuyAmount(data.valueWithoutPrefix)
        }
    }

    private fun hasInputError(): Boolean = binding.errorTextView.isVisible
}
