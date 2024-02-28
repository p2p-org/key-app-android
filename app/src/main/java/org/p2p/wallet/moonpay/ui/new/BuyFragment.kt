package org.p2p.wallet.moonpay.ui.new

import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import org.p2p.core.analytics.constants.ScreenNames
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.uikit.components.FocusField
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentBuyBinding
import org.p2p.wallet.home.ui.select.bottomsheet.BuySelectTokenBottomSheet
import org.p2p.wallet.home.ui.select.bottomsheet.SelectCurrencyBottomSheet
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.moonpay.model.BuyDetailsState
import org.p2p.wallet.moonpay.model.BuyViewData
import org.p2p.wallet.moonpay.model.MoonpayWidgetUrlBuilder
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.moonpay.repository.sell.FiatCurrency
import org.p2p.wallet.moonpay.ui.bottomsheet.BuyDetailsBottomSheet
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.getDrawableCompat
import org.p2p.wallet.utils.getParcelableCompat
import org.p2p.wallet.utils.getSerializableCompat
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TOKEN = "EXTRA_TOKEN"
private const val EXTRA_FIAT_TOKEN = "EXTRA_FIAT_TOKEN"
private const val EXTRA_FIAT_AMOUNT = "EXTRA_AMOUNT"
private const val EXTRA_PRESELECTED_METHOD_TYPE = "EXTRA_PRESELECTED_METHOD_TYPE"

private const val KEY_REQUEST = "KEY_REQUEST_NEW_BUY"
private const val KEY_RESULT_TOKEN = "KEY_RESULT_TOKEN"
private const val KEY_RESULT_CURRENCY = "KEY_RESULT_CURRENCY"

class BuyFragment :
    BaseMvpFragment<BuyContract.View, BuyContract.Presenter>(R.layout.fragment_buy),
    BuyContract.View {

    companion object {
        fun create(
            token: Token,
            fiatToken: String? = null,
            fiatAmount: String? = null,
            preselectedMethodType: PaymentMethod.MethodType? = null
        ): BuyFragment =
            BuyFragment()
                .withArgs(
                    EXTRA_TOKEN to token,
                    EXTRA_FIAT_TOKEN to fiatToken,
                    EXTRA_FIAT_AMOUNT to fiatAmount,
                    EXTRA_PRESELECTED_METHOD_TYPE to preselectedMethodType
                )
    }

    private val token: Token by args(EXTRA_TOKEN)
    private val fiatToken: String? by args(EXTRA_FIAT_TOKEN)
    private val fiatAmount: String? by args(EXTRA_FIAT_AMOUNT)
    private val preselectedMethodType: PaymentMethod.MethodType? by args(EXTRA_PRESELECTED_METHOD_TYPE)

    override val presenter: BuyContract.Presenter by inject {
        parametersOf(
            token,
            fiatToken,
            fiatAmount,
            preselectedMethodType
        )
    }

    private val binding: FragmentBuyBinding by viewBinding()
    private val adapter: PaymentMethodsAdapter by unsafeLazy {
        PaymentMethodsAdapter(presenter::onPaymentMethodSelected)
    }

    private var backPressedCallback: OnBackPressedCallback? = null

    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val tokenKeyProvider: TokenKeyProvider by inject()
    private val moonpayWidgetUrlBuilder: MoonpayWidgetUrlBuilder by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backPressedCallback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            presenter.onBackPressed()
        }

        binding.initViews()
        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST,
            viewLifecycleOwner,
            ::onFragmentResult
        )
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        when {
            result.containsKey(KEY_RESULT_TOKEN) -> {
                result.getParcelableCompat<Token>(KEY_RESULT_TOKEN)?.let {
                    with(binding) {
                        val symbol = it.tokenSymbol
                        amountsView.tokenSymbol = symbol
                        toolbarBuy.title = getString(R.string.buy_toolbar_title, symbol)
                        buttonBuy.text = getString(R.string.buy_toolbar_title, symbol)
                    }
                    presenter.setTokenToBuy(it)
                }
            }
            result.containsKey(KEY_RESULT_CURRENCY) -> {
                result.getSerializableCompat<FiatCurrency>(KEY_RESULT_CURRENCY)?.let {
                    setCurrencyCode(it.abbreviation)
                    presenter.setCurrency(it)
                }
            }
        }
    }

    private fun FragmentBuyBinding.initViews() {
        recyclerViewMethods.adapter = adapter

        toolbarBuy.title = getString(R.string.buy_toolbar_title, token.tokenSymbol)
        toolbarBuy.setNavigationIcon(R.drawable.ic_toolbar_back)
        toolbarBuy.setNavigationOnClickListener { popBackStack() }

        textViewTotalValue.setOnClickListener {
            presenter.onTotalClicked()
        }

        buttonBuy.text = getString(R.string.buy_toolbar_title, token.tokenSymbol)

        amountsView.apply {
            tokenSymbol = this@BuyFragment.token.tokenSymbol
            currencyCode = FiatCurrency.USD.abbreviation.uppercase()

            setOnSelectTokenClickListener(presenter::onSelectTokenClicked)
            setOnTokenAmountChangeListener { amount -> presenter.setBuyAmount(amount, isDelayEnabled = false) }

            setOnSelectCurrencyClickListener(presenter::onSelectCurrencyClicked)
            setOnCurrencyAmountChangeListener { amount -> presenter.setBuyAmount(amount, isDelayEnabled = false) }

            setOnFocusChangeListener(presenter::onFocusFieldChanged)
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
        BuySelectTokenBottomSheet.show(
            fm = childFragmentManager,
            title = getString(R.string.buy_select_token_title),
            tokensToBuy = tokensToBuy,
            preselectedToken = selectedToken,
            requestKey = KEY_REQUEST,
            resultKey = KEY_RESULT_TOKEN
        )
    }

    override fun showCurrency(currencies: List<FiatCurrency>, selectedCurrency: FiatCurrency) {
        SelectCurrencyBottomSheet.show(
            fm = childFragmentManager,
            title = getString(R.string.buy_select_currency_title),
            preselectedCurrency = selectedCurrency,
            currencies = currencies,
            requestKey = KEY_REQUEST,
            resultKey = KEY_RESULT_CURRENCY
        )
    }

    override fun setCurrencyCode(selectedCurrencyCode: String) {
        binding.amountsView.currencyCode = selectedCurrencyCode.uppercase()
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
        val focusField = binding.amountsView.focusField
        if (focusField == FocusField.TOKEN) {
            binding.amountsView.setCurrencyAmount(viewData.receiveAmountText)
        } else {
            binding.amountsView.setTokenAmount(viewData.receiveAmountText)
        }
        binding.textViewTotalValue.text = viewData.totalText
    }

    override fun showDetailsBottomSheet(buyDetailsState: BuyDetailsState) {
        BuyDetailsBottomSheet.show(
            fm = childFragmentManager,
            title = getString(R.string.buy_transaction_details_bottom_sheet_title),
            state = buyDetailsState
        )
    }

    override fun setContinueButtonEnabled(isEnabled: Boolean) {
        binding.buttonBuy.isEnabled = isEnabled
        with(binding.buttonBuy) {
            if (isEnabled) {
                setTextColorRes(R.color.text_snow)
                setBackgroundColor(getColor(R.color.bg_night))
            } else {
                setTextColorRes(R.color.text_mountain)
                setBackgroundColor(getColor(R.color.bg_rain))
                strokeWidth = 1
            }
        }
    }

    override fun clearOppositeFieldAndTotal(totalText: String) {
        when (binding.amountsView.focusField) {
            FocusField.TOKEN -> binding.amountsView.setCurrencyAmount(currencyAmount = null)
            FocusField.CURRENCY -> binding.amountsView.setTokenAmount(tokenAmount = null)
        }

        binding.textViewTotalValue.text = totalText
    }

    override fun navigateToMoonpay(
        amount: String,
        selectedToken: Token,
        selectedCurrency: FiatCurrency,
        paymentMethod: String?
    ) {
        val solSymbol = Constants.SOL_SYMBOL.lowercase()
        val selectedTokenSymbol = selectedToken.tokenSymbol.lowercase()
        val tokenSymbol = if (selectedToken.isSOL) solSymbol else "${selectedTokenSymbol}_$solSymbol"
        val url = moonpayWidgetUrlBuilder.buildBuyWidgetUrl(
            amount = amount,
            walletAddress = tokenKeyProvider.publicKey,
            tokenSymbol = tokenSymbol,
            currencyCode = selectedCurrency.abbreviation.lowercase(),
            paymentMethod = paymentMethod
        )
        try {
            requireContext().showUrlInCustomTabs(url)
            analyticsInteractor.logScreenOpenEvent(ScreenNames.Buy.EXTERNAL)
        } catch (error: Throwable) {
            Timber.e(error, "Unable to open moonpay widget in tabs")
        }
    }

    override fun close() {
        popBackStack()
    }

    override fun onDetach() {
        backPressedCallback?.remove()
        super.onDetach()
    }
}
