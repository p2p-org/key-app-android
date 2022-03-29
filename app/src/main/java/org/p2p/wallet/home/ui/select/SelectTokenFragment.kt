package org.p2p.wallet.home.ui.select

import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSelectTokenBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.showSoftKeyboard
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_ALL_TOKENS = "EXTRA_ALL_TOKENS"
private const val EXTRA_REQUEST_KEY = "EXTRA_REQUEST_KEY"
private const val EXTRA_RESULT_KEY = "EXTRA_RESULT_KEY"

class SelectTokenFragment :
    BaseMvpFragment<SelectTokenContract.View, SelectTokenContract.Presenter>(R.layout.fragment_select_token),
    SelectTokenContract.View,
    SearchView.OnQueryTextListener {

    companion object {
        fun create(tokens: List<Token>, requestKey: String, resultKey: String) = SelectTokenFragment()
            .withArgs(
                EXTRA_ALL_TOKENS to tokens,
                EXTRA_REQUEST_KEY to requestKey,
                EXTRA_RESULT_KEY to resultKey
            )
    }

    override val presenter: SelectTokenContract.Presenter by inject {
        parametersOf(tokens)
    }

    private val tokens: List<Token> by args(EXTRA_ALL_TOKENS)
    private val resultKey: String by args(EXTRA_RESULT_KEY)
    private val requestKey: String by args(EXTRA_REQUEST_KEY)
    private val buyAnalytics: BuyAnalytics by inject()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val binding: FragmentSelectTokenBinding by viewBinding()

    private val tokenAdapter: SelectTokenAdapter by lazy {
        SelectTokenAdapter {
            setFragmentResult(requestKey, bundleOf(resultKey to it))
            parentFragmentManager.popBackStack()
            buyAnalytics.logBuyTokenChosen(it.tokenSymbol, analyticsInteractor.getPreviousScreenName())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            inflateSearchMenu(toolbar)
            tokenRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            tokenRecyclerView.attachAdapter(tokenAdapter)

            val isEmpty = tokens.isEmpty()
            tokenRecyclerView.isVisible = !isEmpty
            emptyTextView.isVisible = isEmpty
            presenter.load()
        }
    }

    override fun showTokens(items: List<Token>) {
        tokenAdapter.setItems(items)
    }

    override fun onQueryTextSubmit(query: String?): Boolean = false

    override fun onQueryTextChange(searchText: String): Boolean {
        presenter.search(searchText)
        return true
    }

    private fun inflateSearchMenu(toolbar: Toolbar) {
        toolbar.inflateMenu(R.menu.menu_search)

        val search = toolbar.menu.findItem(R.id.menu_search)
        val searchView = search.actionView as SearchView

        searchView.apply {
            this.onActionViewExpanded()
            setOnQueryTextListener(this@SelectTokenFragment)
        }
        searchView.showSoftKeyboard()
    }
}
