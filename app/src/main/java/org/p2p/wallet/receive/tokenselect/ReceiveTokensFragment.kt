package org.p2p.wallet.receive.tokenselect

import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import org.koin.android.ext.android.inject
import org.p2p.core.glide.GlideManager
import org.p2p.core.utils.hideKeyboard
import org.p2p.core.utils.insets.appleBottomInsets
import org.p2p.core.utils.insets.appleTopInsets
import org.p2p.core.utils.insets.consume
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.finance_block.financeBlockCellDelegate
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.showSoftKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.recycler.EndlessScrollListener
import org.p2p.wallet.databinding.FragmentReceiveSupportedTokensBinding
import org.p2p.wallet.receive.tokenselect.models.ReceiveTokenPayload
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

private const val IMAGE_SIZE_DP = 44

class ReceiveTokensFragment :
    BaseMvpFragment<ReceiveTokensContract.View, ReceiveTokensContract.Presenter>(
        R.layout.fragment_receive_supported_tokens
    ),
    ReceiveTokensContract.View,
    SearchView.OnQueryTextListener {

    companion object {
        fun create(): ReceiveTokensFragment = ReceiveTokensFragment()
    }

    private val binding: FragmentReceiveSupportedTokensBinding by viewBinding()

    private val glideManager: GlideManager by inject()

    override val presenter: ReceiveTokensContract.Presenter by inject()

    private val adapter = ReceiveTokensAdapter(
        financeBlockCellDelegate(inflateListener = { financeBlock ->
            financeBlock.setOnClickAction { _, item -> onTokenClick(item) }
        })
    )
    private val linearLayoutManager by lazy { LinearLayoutManager(requireContext()) }

    private val scrollListener by lazy {
        EndlessScrollListener(
            layoutManager = linearLayoutManager,
            loadNextPage = { presenter.load(isRefresh = false) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }
        inflateSearchMenu(binding.toolbar)

        with(binding.recyclerViewTokens) {
            layoutManager = linearLayoutManager
            attachAdapter(this@ReceiveTokensFragment.adapter)

            clearOnScrollListeners()
            addOnScrollListener(scrollListener)
        }
        presenter.load(isRefresh = true)
    }

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { view, insets, _ ->
            insets.systemAndIme().consume {
                view.appleTopInsets(this)
                binding.recyclerViewTokens.appleBottomInsets(this)
            }
        }
    }

    private fun inflateSearchMenu(toolbar: Toolbar) {
        val search = toolbar.menu.findItem(R.id.menu_search)
        val searchView = search.actionView as SearchView

        searchView.apply {
            onActionViewExpanded()
            setOnQueryTextListener(this@ReceiveTokensFragment)
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

    override fun setBannerTokens(firstTokenUrl: String, secondTokenUrl: String) = with(binding) {
        imageViewFirstIcon.setTokenIconUrl(firstTokenUrl)
        imageViewSecondIcon.setTokenIconUrl(secondTokenUrl)
    }

    override fun showTokenItems(items: List<AnyCellItem>, scrollToUp: Boolean) {
        with(binding) {
            adapter.items = items
            if (scrollToUp) {
                recyclerViewTokens.smoothScrollToPosition(0)
            }
        }
    }

    override fun showEmptyState(isEmpty: Boolean) = with(binding) {
        binding.textViewEmpty.isVisible = isEmpty
        binding.recyclerViewTokens.isVisible = !isEmpty
    }

    override fun setBannerVisibility(isVisible: Boolean) {
        binding.layoutReceiveBanner.isVisible = isVisible
    }

    override fun resetScrollPosition() {
        scrollListener.reset()
        binding.recyclerViewTokens.smoothScrollToPosition(0)
    }

    private fun ImageView.setTokenIconUrl(tokenIconUrl: String) {
        glideManager.load(
            imageView = this,
            url = tokenIconUrl,
            size = IMAGE_SIZE_DP,
            circleCrop = true
        )
    }

    private fun onTokenClick(item: FinanceBlockCellModel) {
        val tokenDataPayload = (item.payload as? ReceiveTokenPayload) ?: return
        presenter.onTokenClicked(tokenDataPayload)
    }
}
