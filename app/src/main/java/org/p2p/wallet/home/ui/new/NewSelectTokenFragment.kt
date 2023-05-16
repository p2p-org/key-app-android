package org.p2p.wallet.home.ui.new

import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.token.Token
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.showSoftKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSelectTokenNewBinding
import org.p2p.wallet.home.model.SelectTokenItem
import org.p2p.wallet.home.ui.new.adapter.NewSelectTokenAdapter
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.newsend.ui.stub.SendNoAccountFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_SELECTABLE_TOKENS = "ARG_ALL_TOKENS"
private const val ARG_SELECTED_TOKEN = "ARG_SELECTED_TOKEN"
private const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"
private const val ARG_RESULT_KEY = "ARG_RESULT_KEY"
private const val ARG_TITLE = "ARG_TITLE"

class NewSelectTokenFragment :
    BaseMvpFragment<NewSelectTokenContract.View, NewSelectTokenContract.Presenter>(R.layout.fragment_select_token_new),
    NewSelectTokenContract.View,
    SearchView.OnQueryTextListener {

    companion object {
        fun create(
            tokensToSelectFrom: List<Token.Active>? = null,
            selectedToken: Token.Active? = null,
            requestKey: String,
            resultKey: String,
            title: String? = null
        ) = NewSelectTokenFragment()
            .withArgs(
                ARG_SELECTABLE_TOKENS to tokensToSelectFrom,
                ARG_SELECTED_TOKEN to selectedToken?.mintAddress,
                ARG_REQUEST_KEY to requestKey,
                ARG_RESULT_KEY to resultKey,
                ARG_TITLE to title
            )
    }

    private val selectableTokens: List<Token.Active>? by args(ARG_SELECTABLE_TOKENS)
    private val selectedTokenMintAddress: String? by args(ARG_SELECTED_TOKEN)
    private val resultKey: String by args(ARG_RESULT_KEY)
    private val requestKey: String by args(ARG_REQUEST_KEY)
    private val title: String? by args(ARG_TITLE)

    override val presenter: NewSelectTokenContract.Presenter by inject {
        parametersOf(selectedTokenMintAddress, selectableTokens)
    }

    private val buyAnalytics: BuyAnalytics by inject()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val binding: FragmentSelectTokenNewBinding by viewBinding()

    private val tokenAdapter: NewSelectTokenAdapter by unsafeLazy {
        NewSelectTokenAdapter(
            onItemClicked = {
                setFragmentResult(requestKey, bundleOf(resultKey to it))
                buyAnalytics.logBuyTokenChosen(it.tokenSymbol, analyticsInteractor.getPreviousScreenName())
                closeFragment()
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            if (title.isNullOrEmpty()) {
                toolbar.setTitle(R.string.send_pick_token_title)
            } else {
                toolbar.title = title
            }
            toolbar.setNavigationOnClickListener { popBackStack() }
            inflateSearchMenu(toolbar)
            recyclerViewTokens.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewTokens.attachAdapter(tokenAdapter)
        }
    }

    override fun showTokens(items: List<SelectTokenItem>) {
        tokenAdapter.setItems(items)
    }

    override fun scrollToTop() {
        binding.recyclerViewTokens.layoutManager?.scrollToPosition(0)
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

        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }
    }

    private fun closeFragment() {
        // verifying if previous screen is `SendNoAccountFragment`
        // if popBackStack returns true then we popped successfully
        if (popBackStackTo(target = SendNoAccountFragment::class, inclusive = true)) return

        // for all other flows and cases just popping current screen
        popBackStack(hideKeyboard = false)
    }
}
