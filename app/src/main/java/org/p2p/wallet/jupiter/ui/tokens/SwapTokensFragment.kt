package org.p2p.wallet.jupiter.ui.tokens

import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.utils.hideKeyboard
import org.p2p.core.utils.insets.appleBottomInsets
import org.p2p.core.utils.insets.appleTopInsets
import org.p2p.core.utils.insets.consume
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.showSoftKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentJupiterSwapTokensBinding
import org.p2p.wallet.jupiter.analytics.JupiterSwapMainScreenAnalytics
import org.p2p.wallet.jupiter.ui.tokens.adapter.SwapTokensARoundedItemDecoration
import org.p2p.wallet.jupiter.ui.tokens.adapter.SwapTokensAdapter
import org.p2p.wallet.jupiter.ui.tokens.adapter.SwapTokensBRoundedItemDecoration
import org.p2p.wallet.jupiter.ui.tokens.adapter.SwapTokensOtherGroupDividerDecoration
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_CHANGE_TOKEN = "ARG_CHANGE_TOKEN"
private const val ARG_STATE_MANAGE_KEY = "ARG_STATE_MANAGE_KEY"

class SwapTokensFragment :
    BaseMvpFragment<SwapTokensContract.View, SwapTokensContract.Presenter>(R.layout.fragment_jupiter_swap_tokens),
    SwapTokensContract.View,
    SearchView.OnQueryTextListener {

    companion object {
        fun create(tokenToChange: SwapTokensListMode, stateManagerKey: String): SwapTokensFragment =
            SwapTokensFragment()
                .withArgs(
                    ARG_CHANGE_TOKEN to tokenToChange,
                    ARG_STATE_MANAGE_KEY to stateManagerKey,
                )
    }

    private val binding: FragmentJupiterSwapTokensBinding by viewBinding()

    private val tokenToChange: SwapTokensListMode by args(ARG_CHANGE_TOKEN)
    private val stateManagerKey: String by args(ARG_STATE_MANAGE_KEY)

    override val presenter: SwapTokensContract.Presenter by inject { parametersOf(tokenToChange, stateManagerKey) }

    private val analytics: JupiterSwapMainScreenAnalytics by inject()

    private val adapter: SwapTokensAdapter by unsafeLazy {
        SwapTokensAdapter(onTokenClicked = {
            analytics.logTokenChanged(tokenToChange, it)
            presenter.onTokenClicked(it)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setTitle(
            if (tokenToChange == SwapTokensListMode.TOKEN_A) {
                R.string.swap_tokens_token_a_toolbar
            } else {
                R.string.swap_tokens_token_b_toolbar
            }
        )
        binding.toolbar.setNavigationOnClickListener {
            analytics.logSelectTokenClosed(tokenToChange)
            popBackStack()
        }
        inflateSearchMenu(binding.toolbar)

        with(binding.recyclerViewTokens) {
            layoutManager = LinearLayoutManager(requireContext())
            attachAdapter(this@SwapTokensFragment.adapter)

            when (tokenToChange) {
                SwapTokensListMode.TOKEN_A -> {
                    addItemDecoration(SwapTokensARoundedItemDecoration())
                    addItemDecoration(SwapTokensOtherGroupDividerDecoration())
                }
                SwapTokensListMode.TOKEN_B -> {
                    addItemDecoration(SwapTokensBRoundedItemDecoration())
                }
            }
        }
    }

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { view, insets, _ ->
            insets.systemAndIme().consume {
                view.appleTopInsets(this)
                binding.recyclerViewTokens.appleBottomInsets(this)
            }
        }
    }

    // todo: extract SearchView to UiKitToolbar
    private fun inflateSearchMenu(toolbar: Toolbar) {
        val search = toolbar.menu.findItem(R.id.menu_search)
        val searchView = search.actionView as SearchView

        searchView.apply {
            onActionViewExpanded()
            setOnQueryTextListener(this@SwapTokensFragment)
            showSoftKeyboard()
        }

        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean = false

    override fun onQueryTextChange(searchText: String): Boolean {
        presenter.onSearchTokenQueryChanged(searchText)
        return true
    }

    override fun setTokenItems(items: List<AnyCellItem>) {
        adapter.setTokenItems(items)
    }

    override fun showEmptyState(isEmpty: Boolean) {
        binding.textViewEmpty.isVisible = isEmpty
        binding.recyclerViewTokens.isVisible = !isEmpty
    }

    override fun renderLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    override fun close() {
        popBackStack()
    }
}
