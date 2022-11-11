package org.p2p.wallet.send.ui.search

import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSearchBinding
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.ui.main.KEY_REQUEST_SEND
import org.p2p.wallet.send.ui.search.adapter.SearchAdapter
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class SearchFragment :
    BaseMvpFragment<SearchContract.View, SearchContract.Presenter>(R.layout.fragment_search),
    SearchContract.View {

    companion object {
        const val EXTRA_SEARCH_RESULT = "EXTRA_SEARCH_RESULT"
        private const val EXTRA_USERNAMES = "EXTRA_USERNAMES"

        fun create(usernames: List<SearchResult>? = null): SearchFragment =
            SearchFragment()
                .withArgs(EXTRA_USERNAMES to usernames)
    }

    private val usernames: List<SearchResult>? by args(EXTRA_USERNAMES)
    override val presenter: SearchContract.Presenter by inject { parametersOf(usernames) }
    private val binding: FragmentSearchBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private lateinit var textWatcher: TextWatcher

    private val searchAdapter: SearchAdapter by unsafeLazy {
        SearchAdapter(
            onItemClicked = presenter::onSearchResultClick,
            usernameDomainFeatureToggle = get()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Send.RECIPIENT_ADDRESS)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }

        with(binding) {
            backImageView.setOnClickListener { popBackStack() }
            clearImageView.setOnClickListener { searchEditText.text.clear() }
            textWatcher = searchEditText.doAfterTextChanged {
                onSearchQueryChanged(it?.toString().orEmpty())
            }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.attachAdapter(searchAdapter)

            searchEditText.focusAndShowKeyboard()
        }

        presenter.loadInitialData()
    }

    private fun onSearchQueryChanged(newQuery: String) {
        presenter.search(newQuery)

        binding.clearImageView.isVisible = newQuery.isNotEmpty()
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressBar.isInvisible = !isLoading
    }

    override fun showSearchValue(value: String) {
        with(binding.searchEditText) {
            removeTextChangedListener(textWatcher)
            setText(value)
            addTextChangedListener(textWatcher)
        }
    }

    override fun showSearchResult(result: List<SearchResult>) {
        searchAdapter.setItems(result)
    }

    override fun showMessage(textRes: Int?) {
        if (textRes == null) {
            binding.messageTextView.isVisible = false
        } else {
            binding.messageTextView.setText(textRes)
            binding.messageTextView.isVisible = true
        }
    }

    override fun submitSearchResult(searchResult: SearchResult) {
        setFragmentResult(
            KEY_REQUEST_SEND,
            bundleOf(
                EXTRA_SEARCH_RESULT to searchResult
            )
        )
        parentFragmentManager.popBackStack()
    }
}
