package org.p2p.wallet.home.ui.new

import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.showSoftKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSelectTokenNewBinding
import org.p2p.wallet.home.model.SelectTokenItem
import org.p2p.core.token.Token
import org.p2p.wallet.home.ui.new.adapter.NewSelectTokenAdapter
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_ALL_TOKENS = "ARG_ALL_TOKENS"
private const val ARG_SELECTED_TOKEN = "ARG_SELECTED_TOKEN"
private const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"
private const val ARG_RESULT_KEY = "ARG_RESULT_KEY"

class NewSelectTokenFragment :
    BaseMvpFragment<NewSelectTokenContract.View, NewSelectTokenContract.Presenter>(R.layout.fragment_select_token_new),
    NewSelectTokenContract.View,
    SearchView.OnQueryTextListener {

    companion object {
        fun create(tokens: List<Token.Active>, selectedToken: Token.Active?, requestKey: String, resultKey: String) =
            NewSelectTokenFragment()
                .withArgs(
                    ARG_ALL_TOKENS to tokens,
                    ARG_SELECTED_TOKEN to selectedToken,
                    ARG_REQUEST_KEY to requestKey,
                    ARG_RESULT_KEY to resultKey
                )
    }

    private val tokens: List<Token.Active> by args(ARG_ALL_TOKENS)
    private val selectedToken: Token.Active? by args(ARG_SELECTED_TOKEN)

    override val presenter: NewSelectTokenContract.Presenter by inject()

    private val resultKey: String by args(ARG_RESULT_KEY)
    private val requestKey: String by args(ARG_REQUEST_KEY)
    private val buyAnalytics: BuyAnalytics by inject()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val binding: FragmentSelectTokenNewBinding by viewBinding()

    override val navBarColor: Int = R.color.bg_smoke
    override val statusBarColor: Int = R.color.bg_smoke

    private val tokenAdapter: NewSelectTokenAdapter by unsafeLazy {
        NewSelectTokenAdapter(
            onItemClicked = {
                setFragmentResult(requestKey, bundleOf(resultKey to it))
                parentFragmentManager.popBackStack()
                buyAnalytics.logBuyTokenChosen(it.tokenSymbol, analyticsInteractor.getPreviousScreenName())
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            inflateSearchMenu(toolbar)
            recyclerViewTokens.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewTokens.attachAdapter(tokenAdapter)

            presenter.load(tokens, selectedToken)
        }
    }

    override fun showTokens(items: List<SelectTokenItem>) {
        tokenAdapter.setItems(items)
    }

    override fun clearTokens() {
        tokenAdapter.clear()
    }

    override fun showEmptyState(isVisible: Boolean) {
        binding.textViewEmpty.isVisible = isVisible
    }

    override fun onQueryTextSubmit(query: String?): Boolean = false

    override fun onQueryTextChange(searchText: String): Boolean {
        presenter.search(searchText.lowercase())
        return true
    }

    private fun inflateSearchMenu(toolbar: Toolbar) {
        val search = toolbar.menu.findItem(R.id.menu_search)
        val searchView = search.actionView as SearchView

        searchView.apply {
            onActionViewExpanded()
            setOnQueryTextListener(this@NewSelectTokenFragment)
        }
        searchView.showSoftKeyboard()
    }
}
