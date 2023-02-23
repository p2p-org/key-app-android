package org.p2p.wallet.swap.ui.jupiter.tokens

import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.showSoftKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentJupiterSwapTokensBinding
import org.p2p.wallet.swap.ui.jupiter.tokens.adapter.SwapTokensAdapter
import org.p2p.wallet.swap.ui.jupiter.tokens.presenter.SwapTokensListMode
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_CHANGE_TOKEN = "ARG_CHANGE_TOKEN"

class SwapTokensFragment :
    BaseMvpFragment<SwapTokensContract.View, SwapTokensContract.Presenter>(R.layout.fragment_jupiter_swap_tokens),
    SwapTokensContract.View,
    SearchView.OnQueryTextListener {

    companion object {
        fun create(tokenToChange: SwapTokensListMode): SwapTokensFragment =
            SwapTokensFragment()
                .withArgs(ARG_CHANGE_TOKEN to tokenToChange)
    }

    private val binding: FragmentJupiterSwapTokensBinding by viewBinding()

    private val tokenToChange: SwapTokensListMode by args(ARG_CHANGE_TOKEN)

    override val presenter: SwapTokensContract.Presenter by inject { parametersOf(tokenToChange) }

    private val adapter: SwapTokensAdapter by unsafeLazy {
        SwapTokensAdapter(
            onTokenClicked = presenter::onTokenClicked
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }
        inflateSearchMenu(binding.toolbar)

        binding.recyclerViewTokens.attachAdapter(adapter)
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
}
