package org.p2p.wallet.home.ui.select.bottomsheet

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.ui.bottomsheet.BaseRecyclerDoneBottomSheet
import org.p2p.wallet.common.ui.recycler.adapter.DividerItemDecorator
import org.p2p.core.token.Token
import org.p2p.wallet.home.ui.select.NewSelectTokenAdapter
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.withArgs

private const val ARG_ALL_TOKENS = "ARG_ALL_TOKENS"
private const val ARG_SELECTED_TOKEN = "ARG_SELECTED_TOKEN"

class NewSelectTokenBottomSheet : BaseRecyclerDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            title: String,
            tokens: List<Token>,
            preselectedToken: Token? = null,
            requestKey: String,
            resultKey: String
        ) = NewSelectTokenBottomSheet().withArgs(
            ARG_TITLE to title,
            ARG_ALL_TOKENS to tokens,
            ARG_SELECTED_TOKEN to preselectedToken,
            ARG_REQUEST_KEY to requestKey,
            ARG_RESULT_KEY to resultKey
        ).show(fm, NewSelectTokenBottomSheet::javaClass.name)
    }

    private val tokens: List<Token> by args(ARG_ALL_TOKENS)
    private val preselectedToken: Token? by args(ARG_SELECTED_TOKEN)

    private val buyAnalytics: BuyAnalytics by inject()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val tokenAdapter: NewSelectTokenAdapter by unsafeLazy {
        NewSelectTokenAdapter(preselectedItem = preselectedToken, onItemClicked = {
            setFragmentResult(requestKey, bundleOf(resultKey to it))
            dismissAllowingStateLoss()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(recyclerBinding) {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.addItemDecoration(
                DividerItemDecorator(requireContext())
            )
            recyclerView.attachAdapter(tokenAdapter)
            tokenAdapter.setItems(tokens)
        }
    }

    override fun getResult(): Any? = tokenAdapter.selectedItem?.apply {
        buyAnalytics.logBuyTokenChosen(tokenSymbol, analyticsInteractor.getPreviousScreenName())
    }
}
