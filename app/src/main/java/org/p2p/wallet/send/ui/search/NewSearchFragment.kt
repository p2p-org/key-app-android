package org.p2p.wallet.send.ui.search

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewSearchBinding
import org.p2p.wallet.qr.ui.ScanQrFragment
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.ui.main.SendFragment
import org.p2p.wallet.send.ui.search.adapter.SearchAdapter
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.getClipboardText
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextResOrGone

class NewSearchFragment :
    BaseMvpFragment<NewSearchContract.View, NewSearchContract.Presenter>(R.layout.fragment_new_search),
    NewSearchContract.View {

    companion object {
        private const val EXTRA_USERNAMES = "EXTRA_USERNAMES"

        fun create(preselectedRecipients: List<SearchResult>? = null): NewSearchFragment =
            NewSearchFragment()
                .withArgs(EXTRA_USERNAMES to preselectedRecipients)
    }

    private val usernames: List<SearchResult>? by args(EXTRA_USERNAMES)
    override val presenter: NewSearchContract.Presenter by inject { parametersOf(usernames) }
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

        with(binding) {

            toolbar.apply {
                setSearchMenu(
                    object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean = false

                        override fun onQueryTextChange(newText: String?): Boolean {
                            onSearchQueryChanged(newText.orEmpty())
                            return true
                        }
                    },
                    menuRes = R.menu.menu_search_with_scan,
                    searchHintRes = R.string.search_edittext_hint
                )
                searchView?.setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) toggleSearchView()
                }
                setNavigationOnClickListener { popBackStack() }
                setOnMenuItemClickListener {
                    if (it.itemId == R.id.itemScan) {
                        presenter.onScanClicked()
                        true
                    } else {
                        false
                    }
                }
            }

            buttonScan.setOnClickListener { presenter.onScanClicked() }
            buttonContinue.setOnClickListener {
                presenter.onContinueClicked(
                    toolbar.searchView?.query?.toString().orEmpty()
                )
            }

            buttonPaste.setOnClickListener { pasteText() }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.attachAdapter(searchAdapter)
        }

        presenter.loadInitialData()
    }

    private fun onSearchQueryChanged(newQuery: String) {
        presenter.search(newQuery)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressBar.isInvisible = !isLoading
    }

    override fun showNotFound() = with(binding) {
        textViewNotFoundTitle.isVisible = true
        groupErrorView.isVisible = false
        groupEmptyView.isVisible = false
        recyclerView.isVisible = false
    }

    override fun showEmptyState(isEmpty: Boolean) = with(binding) {
        textViewNotFoundTitle.isVisible = false
        groupErrorView.isVisible = false
        groupEmptyView.isVisible = isEmpty
        recyclerView.isVisible = !isEmpty
    }

    override fun showErrorState(isButtonEnabled: Boolean) = with(binding) {
        groupErrorView.isVisible = true
        buttonContinue.isEnabled = isButtonEnabled
        groupEmptyView.isVisible = false
        recyclerView.isVisible = false
        textViewNotFoundTitle.isVisible = false
    }

    override fun showSearchValue(value: String) {
        binding.toolbar.searchView?.setQuery(value, true)
    }

    override fun showSearchResult(result: List<SearchResult>) {
        searchAdapter.setItems(result)
        showEmptyState(isEmpty = result.isEmpty())
    }

    override fun showMessage(textRes: Int?) {
        binding.messageTextView.withTextResOrGone(textRes)
    }

    override fun submitSearchResult(searchResult: SearchResult) {
        replaceFragment(
            SendFragment.create(address = searchResult.addressState.address)
        )
    }

    override fun showScanner() {
        val target = ScanQrFragment.create { onSearchQueryChanged(it) }
        addFragment(target)
    }

    private fun pasteText() = with(binding) {
        val nameOrAddress = requireContext().getClipboardText(trimmed = true)
        nameOrAddress?.let {
            toolbar.searchView?.setQuery(it, true)
        }
    }
}
