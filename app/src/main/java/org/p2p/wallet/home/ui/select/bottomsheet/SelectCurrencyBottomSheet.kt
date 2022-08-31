package org.p2p.wallet.home.ui.select.bottomsheet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.common.ui.bottomsheet.BaseRecyclerDoneBottomSheet
import org.p2p.wallet.common.ui.recycler.adapter.DividerItemDecorator
import org.p2p.wallet.home.ui.select.SelectCurrencyAdapter
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.utils.withArgs
import java.math.BigDecimal

class SelectCurrencyBottomSheet : BaseRecyclerDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            title: String,
            requestKey: String,
            resultKey: String
        ) = SelectCurrencyBottomSheet().withArgs(
            ARG_TITLE to title,
            ARG_REQUEST_KEY to requestKey,
            ARG_RESULT_KEY to resultKey
        ).show(fm, SelectCurrencyBottomSheet::javaClass.name)
    }

    private val currency: List<BuyCurrency.Currency> = listOf(
        createCurrency("GBP"),
        createCurrency("EUR"),
        createCurrency("USD")
    )

    private val currencyAdapter: SelectCurrencyAdapter by lazy {
        SelectCurrencyAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(recyclerBinding) {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.addItemDecoration(
                DividerItemDecorator(requireContext())
            )
            recyclerView.attachAdapter(currencyAdapter)
            currencyAdapter.setItems(currency)
        }
    }

    override fun getResult(): Any? = currencyAdapter.selectedItem

    private fun createCurrency(code: String): BuyCurrency.Currency = BuyCurrency.Currency(
        code = code,
        minAmount = BigDecimal.ZERO,
        maxAmount = null
    )
}
