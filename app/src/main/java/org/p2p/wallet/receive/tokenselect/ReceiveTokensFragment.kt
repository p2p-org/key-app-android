package org.p2p.wallet.receive.tokenselect

import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import org.koin.android.ext.android.inject
import java.util.Objects
import org.p2p.core.glide.GlideManager
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
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
import org.p2p.uikit.utils.recycler.decoration.groupedRoundingFinanceBlockDecoration
import org.p2p.uikit.utils.recycler.decoration.onePxDividerFinanceBlockDecoration
import org.p2p.uikit.utils.showSoftKeyboard
import org.p2p.uikit.utils.toast
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.recycler.EndlessScrollListener
import org.p2p.wallet.databinding.FragmentReceiveSupportedTokensBinding
import org.p2p.wallet.receive.solana.NewReceiveSolanaFragment
import org.p2p.wallet.receive.tokenselect.dialog.SelectReceiveNetworkBottomSheet
import org.p2p.wallet.receive.tokenselect.models.ReceiveNetwork
import org.p2p.wallet.receive.tokenselect.models.ReceiveTokenPayload
import org.p2p.wallet.utils.getSerializableOrNull
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

private const val IMAGE_SIZE_DP = 44
private const val KEY_REQUEST_NETWORK = "KEY_REQUEST_NETWORK"
private const val KEY_RESULT_NETWORK = "KEY_RESULT_NETWORK"

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

    private val adapter = CommonAnyCellAdapter(
        financeBlockCellDelegate(inflateListener = { financeBlock ->
            financeBlock.setOnClickAction { _, item -> onTokenClick(item) }
        }),
        diffUtilCallback = TokenDiffCallback()
    )
    private val layoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(requireContext())
    }
    private val scrollListener: EndlessScrollListener by lazy {
        EndlessScrollListener(
            layoutManager = layoutManager,
            loadNextPage = { presenter.load(isRefresh = false) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }
        inflateSearchMenu(binding.toolbar)

        with(binding.recyclerViewTokens) {
            attachAdapter(this@ReceiveTokensFragment.adapter)
            addItemDecoration(groupedRoundingFinanceBlockDecoration())
            addItemDecoration(onePxDividerFinanceBlockDecoration(requireContext()))
            doOnAttach {
                layoutManager = this@ReceiveTokensFragment.layoutManager
                addOnScrollListener(scrollListener)
            }
            doOnDetach {
                layoutManager = null
                removeOnScrollListener(scrollListener.also { it.reset() })
            }
        }
        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST_NETWORK,
            viewLifecycleOwner,
            ::onFragmentResult
        )
        presenter.load(isRefresh = true)
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        result.getSerializableOrNull<ReceiveNetwork>(KEY_RESULT_NETWORK)?.let { network ->
            presenter.onNetworkSelected(network)
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

    override fun showTokenItems(items: List<AnyCellItem>) {
        adapter.setItems(items)
    }

    override fun showEmptyState(isEmpty: Boolean) = with(binding) {
        binding.textViewEmpty.isVisible = isEmpty
        binding.recyclerViewTokens.isVisible = !isEmpty
    }

    override fun setBannerVisibility(isVisible: Boolean) {
        binding.layoutReceiveBanner.isVisible = isVisible
    }

    override fun resetView() {
        scrollListener.reset()
        binding.recyclerViewTokens.smoothScrollToPosition(0)
    }

    override fun showSelectNetworkDialog(tokensToShowNetworks: List<Token>) {
        SelectReceiveNetworkBottomSheet.show(
            fm = childFragmentManager,
            title = getString(R.string.receive_network_dialog_title),
            requestKey = KEY_REQUEST_NETWORK,
            resultKey = KEY_RESULT_NETWORK,
            tokensForNetworks = tokensToShowNetworks,
        )
    }

    override fun openReceiveInSolana(tokenData: TokenData) = with(tokenData) {
        replaceFragment(
            NewReceiveSolanaFragment.create(
                solAddress = mintAddress, // TODO make this address wallet sol address!
                logoUrl = iconUrl.orEmpty(),
                tokenSymbol = symbol
            )
        )
    }

    override fun openReceiveInEthereum() {
        // TODO make real implementation
        toast("Receive in Ethereum network should be opened!")
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
        presenter.onTokenClicked(item.typedPayload())
    }
}

private class TokenDiffCallback : DiffUtil.ItemCallback<AnyCellItem>() {

    override fun areItemsTheSame(oldItem: AnyCellItem, newItem: AnyCellItem): Boolean {
        return when {
            oldItem is FinanceBlockCellModel && newItem is FinanceBlockCellModel -> {
                val oldMint = oldItem.typedPayload<ReceiveTokenPayload>().tokenData.mintAddress
                val newMint = newItem.typedPayload<ReceiveTokenPayload>().tokenData.mintAddress
                oldMint == newMint
            }
            else -> oldItem::class == newItem::class
        }
    }

    override fun areContentsTheSame(oldItem: AnyCellItem, newItem: AnyCellItem): Boolean {
        return when {
            oldItem is FinanceBlockCellModel && newItem is FinanceBlockCellModel -> {
                val oldMint = oldItem.typedPayload<ReceiveTokenPayload>().tokenData.mintAddress
                val newMint = newItem.typedPayload<ReceiveTokenPayload>().tokenData.mintAddress
                oldMint == newMint
            }
            else -> Objects.equals(oldItem, newItem)
        }
    }
}
