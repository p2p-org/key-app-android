package org.p2p.wallet.moonpay.ui.new

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.components.FocusField
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewBuyBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.ui.select.bottomsheet.NewSelectTokenBottomSheet
import org.p2p.wallet.home.ui.select.bottomsheet.SelectCurrencyBottomSheet
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.moonpay.api.MoonpayUrlBuilder
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.BuyDetailsState
import org.p2p.wallet.moonpay.model.BuyViewData
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.moonpay.ui.bottomsheet.BuyDetailsBottomSheet
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.getDrawableCompat
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

private const val KEY_REQUEST_TOKEN = "KEY_REQUEST_TOKEN"
private const val KEY_RESULT_TOKEN = "KEY_RESULT_TOKEN"

private const val KEY_REQUEST_CURRENCY = "KEY_REQUEST_CURRENCY"
private const val KEY_RESULT_CURRENCY = "KEY_RESULT_CURRENCY"

class NewBuyFragment :
    BaseMvpFragment<NewBuyContract.View, NewBuyContract.Presenter>(R.layout.fragment_new_buy),
    NewBuyContract.View {

    companion object {
        fun create(token: Token): NewBuyFragment = NewBuyFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    override val presenter: NewBuyContract.Presenter by inject { parametersOf(token) }
    override val navBarColor: Int = R.color.night

    private val token: Token by args(EXTRA_TOKEN)
    private val binding: FragmentNewBuyBinding by viewBinding()
    private val adapter: PaymentMethodsAdapter by unsafeLazy {
        PaymentMethodsAdapter(presenter::onPaymentMethodSelected)
    }

    private var backPressedCallback: OnBackPressedCallback? = null

    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val tokenKeyProvider: TokenKeyProvider by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backPressedCallback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            presenter.onBackPressed()
        }

        binding.initViews()

        setFragmentResultListener(KEY_REQUEST_TOKEN)
        setFragmentResultListener(KEY_REQUEST_CURRENCY)
    }

    private fun setFragmentResultListener(keyResult: String) {
        childFragmentManager.setFragmentResultListener(
            keyResult,
            viewLifecycleOwner,
            ::onFragmentResult
        )
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            KEY_REQUEST_TOKEN -> {
                result.getParcelable<Token>(KEY_RESULT_TOKEN)?.let {
                    with(binding) {
                        val symbol = it.tokenSymbol
                        amountsView.tokenSymbol = symbol
                        toolbarBuy.title = getString(R.string.buy_toolbar_title, symbol)
                        buttonBuy.text = getString(R.string.buy_toolbar_title, symbol)
                    }
                    presenter.setTokenToBuy(it)
                }
            }

            KEY_REQUEST_CURRENCY -> {
                result.getParcelable<BuyCurrency.Currency>(KEY_RESULT_CURRENCY)?.let {
                    setCurrencyCode(it.code)
                    presenter.setCurrency(it)
                }
            }
        }
    }

    private fun FragmentNewBuyBinding.initViews() {
        recyclerViewMethods.adapter = adapter

        toolbarBuy.title = getString(R.string.buy_toolbar_title, token.tokenSymbol)
        toolbarBuy.setNavigationIcon(R.drawable.ic_toolbar_back)
        toolbarBuy.setNavigationOnClickListener { popBackStack() }

        textViewTotal.setOnClickListener {
            presenter.onTotalClicked()
        }

        buttonBuy.text = getString(R.string.buy_toolbar_title, token.tokenSymbol)

        amountsView.apply {
            tokenSymbol = this@NewBuyFragment.token.tokenSymbol
            currencyCode = Constants.USD_READABLE_SYMBOL

            setOnSelectTokenClickListener { presenter.onSelectTokenClicked() }
            setOnTokenAmountChangeListener { amount -> presenter.setBuyAmount(amount, isDelayEnabled = false) }

            setOnSelectCurrencyClickListener { presenter.onSelectCurrencyClicked() }
            setOnCurrencyAmountChangeListener { amount -> presenter.setBuyAmount(amount, isDelayEnabled = false) }

            setOnFocusChangeListener { focusField ->
                presenter.onFocusFieldChanged(focusField)
            }
        }

        buttonBuy.setOnClickListener { presenter.onContinueClicked() }
    }

    override fun showPreselectedAmount(amount: String) {
        binding.amountsView.apply {
            setCurrencyAmount(amount)
            requestFocus(FocusField.CURRENCY)
        }
    }

    override fun showPaymentMethods(methods: List<PaymentMethod>?) {
        adapter.setItems(methods.orEmpty())
        binding.groupPaymentMethods.isVisible = methods != null
    }

    override fun showTokensToBuy(selectedToken: Token, tokensToBuy: List<Token>) {
        NewSelectTokenBottomSheet.show(
            fm = childFragmentManager,
            title = getString(R.string.buy_select_token_title),
            tokens = tokensToBuy,
            preselectedToken = selectedToken,
            requestKey = KEY_REQUEST_TOKEN,
            resultKey = KEY_RESULT_TOKEN
        )
    }

    override fun showCurrency(currencies: List<BuyCurrency.Currency>, selectedCurrency: BuyCurrency.Currency) {
        SelectCurrencyBottomSheet.show(
            fm = childFragmentManager,
            title = getString(R.string.buy_select_currency_title),
            preselectedCurrency = selectedCurrency,
            currencies = currencies,
            requestKey = KEY_REQUEST_CURRENCY,
            resultKey = KEY_RESULT_CURRENCY
        )
    }

    override fun setCurrencyCode(selectedCurrencyCode: String) {
        binding.amountsView.currencyCode = selectedCurrencyCode
    }

    override fun showLoading(isLoading: Boolean) {
        // TODO: https://www.figma.com/file/X5oRVw7OmaFfLLiJMqHiBn/Bank-transfer-Android?node-id=183%3A30478
    }

    override fun showMessage(message: String?, selectedTokenSymbol: String?) = with(binding.buttonBuy) {
        if (message != null) {
            icon = null
            text = message
        } else {
            icon = context.getDrawableCompat(R.drawable.ic_wallet_home)
            text = getString(R.string.buy_toolbar_title, selectedTokenSymbol)
        }
    }

    override fun showTotal(viewData: BuyViewData) {
        val focusField = binding.amountsView.getFocusField()
        if (focusField == FocusField.TOKEN) {
            binding.amountsView.setCurrencyAmount(viewData.receiveAmountText)
        } else {
            binding.amountsView.setTokenAmount(viewData.receiveAmountText)
        }
        binding.textViewTotal.text = viewData.totalText
    }

    override fun showDetailsBottomSheet(buyDetailsState: BuyDetailsState) {
        BuyDetailsBottomSheet.show(
            childFragmentManager,
            getString(R.string.buy_transaction_details_bottom_sheet_title),
            buyDetailsState
        )
    }

    override fun setContinueButtonEnabled(isEnabled: Boolean) {
        binding.buttonBuy.isEnabled = isEnabled
    }

    override fun clearOppositeFieldAndTotal(totalText: String) {
        when (binding.amountsView.getFocusField()) {
            FocusField.TOKEN -> binding.amountsView.setCurrencyAmount(null)
            FocusField.CURRENCY -> binding.amountsView.setTokenAmount(null)
        }

        binding.textViewTotal.text = totalText
    }

    override fun navigateToMoonpay(
        amount: String,
        selectedToken: Token,
        selectedCurrency: BuyCurrency.Currency,
        paymentMethod: String?
    ) {
        val solSymbol = Constants.SOL_SYMBOL.lowercase()
        val selectedTokenSymbol = selectedToken.tokenSymbol.lowercase()
        val tokenSymbol = if (selectedToken.isSOL) solSymbol else "${selectedTokenSymbol}_$solSymbol"
        val url = MoonpayUrlBuilder.build(
            moonpayWalletDomain = requireContext().getString(R.string.moonpayWalletDomain),
            moonpayApiKey = BuildConfig.moonpayKey,
            amount = amount,
            walletAddress = tokenKeyProvider.publicKey,
            tokenSymbol = tokenSymbol,
            currencyCode = selectedCurrency.code.lowercase(),
            paymentMethod = paymentMethod
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
}
