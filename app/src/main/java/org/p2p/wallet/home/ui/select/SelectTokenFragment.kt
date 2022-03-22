package org.p2p.wallet.home.ui.select

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.AnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseFragment
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
private const val QUERY_MIN_LENGTH = 2

class SelectTokenFragment : BaseFragment(R.layout.fragment_select_token), SearchView.OnQueryTextListener {

    companion object {
        fun create(tokens: List<Token>, requestKey: String, resultKey: String) = SelectTokenFragment()
            .withArgs(
                EXTRA_ALL_TOKENS to tokens,
                EXTRA_REQUEST_KEY to requestKey,
                EXTRA_RESULT_KEY to resultKey
            )
    }

    private val tokens: List<Token> by args(EXTRA_ALL_TOKENS)
    private val resultKey: String by args(EXTRA_RESULT_KEY)
    private val requestKey: String by args(EXTRA_REQUEST_KEY)
    private val buyAnalytics: BuyAnalytics by inject()
    private val analyticsInteractor: AnalyticsInteractor by inject()
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
            tokenAdapter.setItems(tokens)

            val isEmpty = tokens.isEmpty()
            tokenRecyclerView.isVisible = !isEmpty
            emptyTextView.isVisible = isEmpty
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean = false

    override fun onQueryTextChange(newText: String?): Boolean {
        val searchText = newText.orEmpty()
        if (searchText.length < QUERY_MIN_LENGTH) {
            tokenAdapter.setItems(tokens)
            return true
        }
        val filteredItems = tokens.filter {
            it.tokenName.startsWith(searchText, ignoreCase = true) || it.tokenSymbol == newText
        }
        tokenAdapter.setItems(filteredItems)
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