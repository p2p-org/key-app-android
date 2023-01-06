package org.p2p.wallet.newsend.ui.search

import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.token.Token
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.atoms.skeleton.UiKitSkeletonLineModel
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewSearchBinding
import org.p2p.wallet.newsend.ui.NewSendFragment
import org.p2p.wallet.qr.ui.ScanQrFragment
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.ui.search.adapter.SearchAdapter
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrInvisible

private const val REQUEST_QR_KEY = "REQUEST_QR_KEY"
private const val RESULT_QR_KEY = "RESULT_QR_KEY"

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

class NewSearchFragment :
    BaseMvpFragment<NewSearchContract.View, NewSearchContract.Presenter>(R.layout.fragment_new_search),
    NewSearchContract.View {

    companion object {
        fun create(): NewSearchFragment = NewSearchFragment()

        fun create(selectedToken: Token.Active): NewSearchFragment =
            NewSearchFragment()
                .withArgs(EXTRA_TOKEN to selectedToken)
    }

    private val selectedToken: Token.Active? by args(EXTRA_TOKEN)

    override val presenter: NewSearchContract.Presenter by inject {
        parametersOf(selectedToken)
    }
    private val binding: FragmentNewSearchBinding by viewBinding()

    override val statusBarColor: Int = R.color.bg_smoke
    override val navBarColor: Int = R.color.bg_smoke

    private val searchAdapter: SearchAdapter by unsafeLazy {
        SearchAdapter(
            onItemClicked = presenter::onSearchResultClick,
            usernameDomainFeatureToggle = get()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }

        setOnResultListener()

        with(binding) {
            binding.toolbar.apply {
                setSearchMenu(searchHintRes = R.string.search_edittext_hint)
                setNavigationOnClickListener { popBackStack() }
                setOnDoneListener { hideKeyboard() }
                onQueryUpdated = { presenter.search(it) }
            }

            buttonScanQr.setOnClickListener { presenter.onScanClicked() }

            recyclerViewSearchResults.apply {
                itemAnimator = null
                layoutManager = LinearLayoutManager(requireContext())
                attachAdapter(searchAdapter)
            }
        }
    }

    override fun updateSearchInput(recentQuery: String, submit: Boolean) {
        binding.toolbar.setQuery(recentQuery, submit)
    }

    override fun showLoading() = with(binding) {
        textViewNotFoundTitle.isVisible = false
        textViewErrorTitle.isVisible = false
        groupEmptyView.isVisible = false
        recyclerViewSearchResults.isVisible = true
        searchAdapter.setItems(listOf(UiKitSkeletonLineModel()))
    }

    override fun showNotFound() = with(binding) {
        textViewNotFoundTitle.isVisible = true
        textViewErrorTitle.isVisible = false
        groupEmptyView.isVisible = false
        recyclerViewSearchResults.isVisible = false
    }

    override fun showEmptyState(isEmpty: Boolean) = with(binding) {
        textViewNotFoundTitle.isVisible = false
        textViewErrorTitle.isVisible = false
        groupEmptyView.isVisible = isEmpty
        recyclerViewSearchResults.isVisible = !isEmpty
    }

    override fun showErrorState() = with(binding) {
        textViewErrorTitle.isVisible = true
        groupEmptyView.isVisible = false
        recyclerViewSearchResults.isVisible = false
        textViewNotFoundTitle.isVisible = false
    }

    override fun showBackgroundVisible(isVisible: Boolean) {
        binding.recyclerViewSearchResults.apply {
            if (isVisible) {
                setBackgroundResource(R.drawable.bg_snow_rounded_16)
            } else {
                background = null
            }
        }
    }

    override fun showUsers(result: List<SearchResult>) {
        searchAdapter.setItems(result)
        showEmptyState(result.isEmpty())
    }

    override fun clearUsers() {
        searchAdapter.clearItems()
    }

    override fun showUsersMessage(textRes: Int?) {
        binding.messageTextView.withTextOrInvisible(textRes)
    }

    override fun submitSearchResult(searchResult: SearchResult, initialToken: Token.Active?) {
        replaceFragment(NewSendFragment.create(recipient = searchResult, initialToken = initialToken))
    }

    override fun showScanner() {
        replaceFragment(ScanQrFragment.create(REQUEST_QR_KEY, RESULT_QR_KEY))
    }

    private fun setOnResultListener() {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            REQUEST_QR_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            bundle.getString(RESULT_QR_KEY)?.let { address ->
                binding.toolbar.setQuery(address, true)
            }
        }
    }
}
