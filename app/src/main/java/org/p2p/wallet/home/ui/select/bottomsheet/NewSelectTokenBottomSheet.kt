package org.p2p.wallet.home.ui.select.bottomsheet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.ui.bottomsheet.BaseDoneBottomSheet
import org.p2p.wallet.common.ui.recycler.adapter.DividerItemDecorator
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.ui.select.NewSelectTokenAdapter
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.withArgs

private const val EXTRA_ALL_TOKENS = "EXTRA_ALL_TOKENS"

class NewSelectTokenBottomSheet : BaseDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            title: String,
            tokens: List<Token>,
            requestKey: String,
            resultKey: String
        ) = NewSelectTokenBottomSheet().withArgs(
            ARG_TITLE to title,
            EXTRA_ALL_TOKENS to tokens,
            ARG_REQUEST_KEY to requestKey,
            ARG_RESULT_KEY to resultKey
        ).show(fm, NewSelectTokenBottomSheet::javaClass.name)
    }

    private val tokens: List<Token> by args(EXTRA_ALL_TOKENS)

    private val buyAnalytics: BuyAnalytics by inject()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val tokenAdapter: NewSelectTokenAdapter by unsafeLazy {
        NewSelectTokenAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.addItemDecoration(
                DividerItemDecorator(requireContext())
            )
            recyclerView.attachAdapter(tokenAdapter)
            tokenAdapter.setItems(tokens)
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun getResult(): Any? = tokenAdapter.selectedItem?.apply {
        buyAnalytics.logBuyTokenChosen(tokenSymbol, analyticsInteractor.getPreviousScreenName())
    }
}
