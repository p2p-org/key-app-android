package org.p2p.wallet.moonpay.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.textwatcher.PrefixSuffixTextWatcher
import org.p2p.wallet.databinding.FragmentBuySolanaBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.moonpay.api.MoonpayUrlBuilder
import org.p2p.wallet.moonpay.model.BuyViewData
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

class BuySolanaFragment :
    BaseMvpFragment<BuySolanaContract.View, BuySolanaContract.Presenter>(R.layout.fragment_buy_solana),
    BuySolanaContract.View {

    companion object {
        fun create(token: Token): BuySolanaFragment = BuySolanaFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    override val presenter: BuySolanaContract.Presenter by inject { parametersOf(token) }
    private val token: Token by args(EXTRA_TOKEN)

    private val binding: FragmentBuySolanaBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val tokenKeyProvider: TokenKeyProvider by inject()

    private var backPressedCallback: OnBackPressedCallback? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        backPressedCallback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            presenter.onBackPressed()
        }
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Buy.SOL)

        binding.initView()

        presenter.loadData()
    }

    private fun FragmentBuySolanaBinding.initView() {
        val tokenToBuySymbol = token.tokenSymbol

        toolbar.title = getString(R.string.buy_token_on_moonpay, tokenToBuySymbol)
        toolbar.setNavigationOnClickListener {
            popBackStack()
        }

        getValueTextView.text = getString(R.string.buy_zero_token, tokenToBuySymbol)
        priceView.labelText = getString(R.string.buy_token_price, tokenToBuySymbol)
        purchaseCostView.labelText = getString(R.string.buy_token_purchase_cost, tokenToBuySymbol)
        accountCreationView.labelText = getString(R.string.buy_account_creation, tokenToBuySymbol)

        installPrefixWatcher()

        continueButton.setOnClickListener {
            presenter.onContinueClicked()
        }
        getValueTextView.setOnClickListener {
            presenter.onSwapClicked()
        }
    }

    override fun showTokenPrice(price: String) {
        binding.priceView.setValueText(price)
    }

    override fun showData(viewData: BuyViewData) = with(binding) {
        priceView.setValueText(viewData.priceText)
        getValueTextView.text = viewData.receiveAmountText
        processingFeeView.setValueText(viewData.processingFeeText)
        networkFeeView.setValueText(viewData.networkFeeText)
        extraFeeView.setValueText(viewData.extraFeeText)
        accountCreationView.setValueText(viewData.accountCreationCostText)
        viewData.purchaseCostText?.let {
            purchaseCostView.setValueText(it)
        }

        totalView.setValueText(viewData.totalText)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressBar.isInvisible = !isLoading
    }

    override fun showMessage(message: String?) {
        binding.errorTextView.withTextOrGone(message)
    }

    override fun setContinueButtonEnabled(isEnabled: Boolean) {
        binding.continueButton.isEnabled = isEnabled && !hasInputError()
    }

    override fun navigateToMoonpay(amount: String) {
        val solSymbol = Constants.SOL_SYMBOL.lowercase()
        val selectedTokenSymbol = token.tokenSymbol.lowercase()
        val tokenSymbol = if (token.isSOL) solSymbol else "${selectedTokenSymbol}_$solSymbol"
        val url = MoonpayUrlBuilder.build(
            moonpayWalletDomain = requireContext().getString(R.string.moonpayWalletDomain),
            moonpayApiKey = BuildConfig.moonpayKey,
            amount = amount,
            walletAddress = tokenKeyProvider.publicKey,
            tokenSymbol = tokenSymbol,
            currencyCode = Constants.USD_READABLE_SYMBOL.lowercase()
        )
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Buy.EXTERNAL)
        requireContext().showUrlInCustomTabs(url)
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
        installPrefixWatcher(isSwapped, prefixSuffixSymbol)

        val isSuffix = prefixSuffixSymbol != Constants.USD_SYMBOL
        payEditText.hint = if (isSuffix) {
            "0 $prefixSuffixSymbol"
        } else {
            "${prefixSuffixSymbol}0"
        }

        // update edit text with already entered text
        // to emit PrefixSuffixTextWatcher
        payEditText.text = payEditText.text
    }

    private fun installPrefixWatcher(
        isSwapped: Boolean = false,
        prefixSuffixSymbol: String = Constants.USD_SYMBOL
    ) {
        with(binding) {
            val isSuffix = prefixSuffixSymbol != Constants.USD_SYMBOL
            val finalPrefixSuffixSymbol = if (isSuffix) " $prefixSuffixSymbol" else prefixSuffixSymbol
            PrefixSuffixTextWatcher.uninstallFrom(payEditText)
            PrefixSuffixTextWatcher.installOn(payEditText, finalPrefixSuffixSymbol, isSuffix = isSuffix) { data ->
                if (!isSwapped) purchaseCostView.setValueText(data.prefixText)
                presenter.setBuyAmount(data.valueWithoutPrefix)
            }
        }
    }

    private fun hasInputError(): Boolean = binding.errorTextView.isVisible
}
