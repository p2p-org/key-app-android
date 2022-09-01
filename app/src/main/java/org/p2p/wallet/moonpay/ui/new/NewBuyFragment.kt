package org.p2p.wallet.moonpay.ui.new

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewBuyBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.ui.select.bottomsheet.NewSelectTokenBottomSheet
import org.p2p.wallet.home.ui.select.bottomsheet.SelectCurrencyBottomSheet
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

private const val KEY_REQUEST_TOKEN = "KEY_REQUEST_TOKEN"
private const val KEY_RESULT_TOKEN = "KEY_RESULT_TOKEN"

private const val KEY_REQUEST_CURRENCY = "KEY_REQUEST_CURRENCY"
private const val KEY_RESULT_CURRENCY = "KEY_RESULT_CURRENCY"

private val PAYMENT_METHODS = listOf(
    PaymentMethod(
        isSelected = true,
        feePercent = 1f,
        paymentPeriodResId = R.string.buy_period_bank_transfer,
        methodResId = R.string.buy_method_bank_transfer,
        iconResId = R.drawable.ic_bank
    ),
    PaymentMethod(
        isSelected = false,
        feePercent = 4.5f,
        paymentPeriodResId = R.string.buy_period_card,
        methodResId = R.string.buy_method_card,
        iconResId = R.drawable.ic_card
    )
)

class NewBuyFragment : BaseMvpFragment<NewBuyContract.View, NewBuyContract.Presenter>(R.layout.fragment_new_buy),
    NewBuyContract.View {

    companion object {
        fun create(token: Token): NewBuyFragment = NewBuyFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    override val presenter: NewBuyContract.Presenter by inject { parametersOf(token) }
    private val token: Token by args(EXTRA_TOKEN)
    private val binding: FragmentNewBuyBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                    binding.amountsView.token = it.tokenSymbol
                }
            }

            KEY_REQUEST_CURRENCY -> {
                result.getParcelable<BuyCurrency.Currency>(KEY_RESULT_CURRENCY)?.let {
                    binding.amountsView.currency = it.code
                }
            }
        }
    }

    private fun FragmentNewBuyBinding.initViews() {
        val adapter = PaymentMethodsAdapter {}.apply { setItems(PAYMENT_METHODS) }
        recyclerViewMethods.adapter = adapter

        toolbarBuy.title = getString(R.string.buy_toolbar_title, token.tokenSymbol)
        toolbarBuy.setNavigationIcon(R.drawable.ic_toolbar_back)
        toolbarBuy.setNavigationOnClickListener { popBackStack() }

        amountsView.token = token.tokenSymbol
        amountsView.currency = "USD"

        buttonBuy.text = getString(R.string.buy_toolbar_title, "SOL")

        initCurrencies()
    }

    override fun initTokensToBuy(tokensToBuy: List<Token>) {
        binding.amountsView.setOnSelectTokenClickListener {
            NewSelectTokenBottomSheet.show(
                fm = childFragmentManager,
                title = getString(R.string.buy_select_token_title),
                tokens = tokensToBuy,
                requestKey = KEY_REQUEST_TOKEN,
                resultKey = KEY_RESULT_TOKEN
            )
        }
    }

    private fun initCurrencies() {
        binding.amountsView.setOnSelectCurrencyClickListener {
            SelectCurrencyBottomSheet.show(
                fm = childFragmentManager,
                title = getString(R.string.buy_select_currency_title),
                requestKey = KEY_REQUEST_CURRENCY,
                resultKey = KEY_RESULT_CURRENCY
            )
        }
    }
}
