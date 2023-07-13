package org.p2p.wallet.receive.list

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.SpanUtils
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.recycler.EndlessScrollListener
import org.p2p.wallet.databinding.FragmentReceiveListBinding
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.core.token.TokenMetadata
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class TokenListFragment :
    BaseMvpFragment<TokenListContract.View, TokenListContract.Presenter>(R.layout.fragment_receive_list),
    TokenListContract.View {

    companion object {
        fun create() = TokenListFragment()
    }

    override val presenter: TokenListContract.Presenter by inject()
    private val binding: FragmentReceiveListBinding by viewBinding()
    private val browseAnalytics: BrowseAnalytics by inject()
    private val adapter = TokenListAdapter()
    private val linearLayoutManager by lazy { LinearLayoutManager(requireContext()) }
    private val scrollListener by lazy {
        EndlessScrollListener(linearLayoutManager, ::loadNextPage, ::onScrollYChanged)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            with(binding.recyclerView) {
                layoutManager = linearLayoutManager
                attachAdapter(this@TokenListFragment.adapter)

                clearOnScrollListeners()
                addOnScrollListener(scrollListener)
            }
            backImageView.setOnClickListener { popBackStack() }
            searchEditText.doAfterTextChanged { text ->
                presenter.search(text = text)
                clearImageView.isInvisible = text.isNullOrEmpty()
            }
            clearImageView.setOnClickListener {
                searchEditText.text = null
            }
            val info = getString(R.string.receive_token_list_info)
            val alert = getString(R.string.receive_token_list_do_not_recommend)
            infoTextView.text = SpanUtils.setTextBold(info, alert)

            searchEditText.focusAndShowKeyboard()
        }
        presenter.load(isRefresh = true)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun showItems(items: List<TokenMetadata>, scrollToUp: Boolean) {
        with(binding) {
            recyclerView.post {
                adapter.setItems(items)
                if (scrollToUp) {
                    recyclerView.smoothScrollToPosition(0)
                }
            }
            recyclerView.isVisible = items.isNotEmpty()
            emptyView.isVisible = items.isEmpty()
        }
    }

    override fun showEmpty(searchText: String) {
        with(binding) {
            val text = getString(R.string.receive_token_list_not_found, searchText)
            emptyTextView.text = text

            recyclerView.isVisible = false
            emptyView.isVisible = true
        }
    }

    override fun reset() {
        scrollListener.reset()
        binding.recyclerView.post {
            binding.recyclerView.smoothScrollToPosition(0)
        }
    }

    private fun loadNextPage(count: Int) {
        presenter.load(isRefresh = false)
    }

    private fun onScrollYChanged(dY: Int) {
        // TODO calculate scroll Deepth
        browseAnalytics.logTokenListScrolled(dY.toString())
    }
}
