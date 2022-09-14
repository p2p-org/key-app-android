package org.p2p.wallet.home.ui.select.bottomsheet

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.common.ui.bottomsheet.BaseRecyclerDoneBottomSheet
import org.p2p.wallet.common.ui.recycler.adapter.DividerItemDecorator
import org.p2p.wallet.home.ui.select.SelectCurrencyAdapter
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.withArgs

private const val ARG_SELECTED_CURRENCY = "ARG_SELECTED_CURRENCY"
private const val ARG_CURRENCIES = "ARG_CURRENCIES"

class SelectCurrencyBottomSheet : BaseRecyclerDoneBottomSheet() {

    companion object {

        val DEFAULT_CURRENCY = BuyCurrency.Currency.create(Constants.USD_READABLE_SYMBOL)

        fun show(
            fm: FragmentManager,
            title: String,
            preselectedCurrency: BuyCurrency.Currency = DEFAULT_CURRENCY,
            currencies: List<BuyCurrency.Currency>,
            requestKey: String,
            resultKey: String
        ) = SelectCurrencyBottomSheet().withArgs(
            ARG_TITLE to title,
            ARG_SELECTED_CURRENCY to preselectedCurrency,
            ARG_CURRENCIES to currencies,
            ARG_REQUEST_KEY to requestKey,
            ARG_RESULT_KEY to resultKey
        ).show(fm, SelectCurrencyBottomSheet::javaClass.name)
    }

    private val currenciesToSelect: List<BuyCurrency.Currency> by args(ARG_CURRENCIES)
    private val preselectedCurrency: BuyCurrency.Currency by args(ARG_SELECTED_CURRENCY)

    private val currencyAdapter: SelectCurrencyAdapter by unsafeLazy {
        SelectCurrencyAdapter(preselectedItem = preselectedCurrency) {
            setFragmentResult(requestKey, bundleOf(resultKey to it))
            dismissAllowingStateLoss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(recyclerBinding) {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.addItemDecoration(
                DividerItemDecorator(requireContext())
            )
            recyclerView.attachAdapter(currencyAdapter)
            currencyAdapter.setItems(currenciesToSelect)
        }
    }

    override fun getResult(): Any? = currencyAdapter.selectedItem
}
