package org.p2p.wallet.moonpay.ui.new

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
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
import org.p2p.wallet.moonpay.model.BuyViewData
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.moonpay.ui.bottomsheet.BuyDetailsBottomSheet
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

private const val KEY_REQUEST_TOKEN = "KEY_REQUEST_TOKEN"
private const val KEY_RESULT_TOKEN = "KEY_RESULT_TOKEN"

private const val KEY_REQUEST_CURRENCY = "KEY_REQUEST_CURRENCY"
private const val KEY_RESULT_CURRENCY = "KEY_RESULT_CURRENCY"

private val buyViewData = BuyViewData(
    tokenSymbol = "SOL",
    currencySymbol = "USD",
    price = 100f.toBigDecimal(),
    receiveAmount = 1500.0,
    5.3f.toBigDecimal(),
    networkFee = 0.1f.toBigDecimal(),
    extraFee = 0f.toBigDecimal(),
    accountCreationCost = 0f.toBigDecimal(),
    total = 1405.4f.toBigDecimal(),
    receiveAmountText = "",
    purchaseCostText = "$ 1 400"
)

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
    private val adapter: PaymentMethodsAdapter by lazy { PaymentMethodsAdapter(presenter::onPaymentMethodSelected) }

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
                        amountsView.token = symbol
                        toolbarBuy.title = getString(R.string.buy_toolbar_title, symbol)
                    }
                    presenter.setToken(it)
                }
            }

            KEY_REQUEST_CURRENCY -> {
                result.getParcelable<BuyCurrency.Currency>(KEY_RESULT_CURRENCY)?.let {
                    binding.amountsView.currency = it.code
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

        amountsView.token = token.tokenSymbol
        amountsView.currency = "USD"

        textViewTotal.setOnClickListener {
            BuyDetailsBottomSheet.show(
                childFragmentManager,
                getString(R.string.buy_transaction_details_bottom_sheet_title),
                buyViewData
            )
        }

        buttonBuy.text = getString(R.string.buy_toolbar_title, "SOL")

        binding.amountsView.apply {
            setOnSelectTokenClickListener { presenter.onSelectTokenClicked() }
            setOnTokenAmountChangeListener { amount -> presenter.setBuyAmount(amount, isDelayEnabled = false) }
        }
        binding.amountsView.apply {
            setOnSelectCurrencyClickListener { presenter.onSelectCurrencyClicked() }
            setOnCurrencyAmountChangeListener { amount -> presenter.setBuyAmount(amount, isDelayEnabled = false) }
        }

        binding.amountsView.setOnFocusChangeListener { focusMode ->
            presenter.onFocusModeChanged(focusMode)
        }
    }

    override fun showPaymentMethods(methods: List<PaymentMethod>) {
        adapter.setItems(methods)
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

    override fun showCurrency(selectedCurrency: BuyCurrency.Currency) {
        SelectCurrencyBottomSheet.show(
            fm = childFragmentManager,
            title = getString(R.string.buy_select_currency_title),
            preselectedCurrency = selectedCurrency,
            requestKey = KEY_REQUEST_CURRENCY,
            resultKey = KEY_RESULT_CURRENCY
        )
    }

    override fun showLoading(isLoading: Boolean) {
        TODO("Not yet implemented")
    }

    override fun showMessage(message: String?) {
        binding.buttonBuy.isEnabled = message == null
        message?.let { binding.buttonBuy.text = it }
    }

    override fun showData(viewData: BuyViewData) {
        binding.textViewTotal.text = viewData.totalText
    }

    override fun setContinueButtonEnabled(isEnabled: Boolean) {
        binding.buttonBuy.isEnabled = isEnabled
    }

    override fun navigateToMoonpay(amount: String) {
        val solSymbol = Constants.SOL_SYMBOL.lowercase()
        val tokenSymbol = token.tokenSymbol.lowercase()
        val currencyCode = if (token.isSOL) solSymbol else "${tokenSymbol}_$solSymbol"
        val url = MoonpayUrlBuilder.build(
            moonpayWalletDomain = requireContext().getString(R.string.moonpayWalletDomain),
            moonpayApiKey = BuildConfig.moonpayKey,
            amount = amount,
            walletAddress = tokenKeyProvider.publicKey,
            currencyCode = currencyCode,
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
