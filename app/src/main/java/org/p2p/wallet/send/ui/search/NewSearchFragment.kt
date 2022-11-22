package org.p2p.wallet.send.ui.search

import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewSearchBinding
import org.p2p.wallet.qr.ui.ScanQrFragment
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.ui.search.adapter.SearchAdapter
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.getClipboardText
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class NewSearchFragment :
    BaseMvpFragment<NewSearchContract.View, NewSearchContract.Presenter>(R.layout.fragment_new_search),
    NewSearchContract.View {

    companion object {
        private const val EXTRA_USERNAMES = "EXTRA_USERNAMES"

        fun create(usernames: List<SearchResult>? = null): NewSearchFragment =
            NewSearchFragment()
                .withArgs(EXTRA_USERNAMES to usernames)
    }

    private val usernames: List<SearchResult>? by args(EXTRA_USERNAMES)
    override val presenter: NewSearchContract.Presenter by inject { parametersOf(usernames) }
    private val binding: FragmentNewSearchBinding by viewBinding()
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
            toolbar.setNavigationOnClickListener { popBackStack() }

            textWatcher = searchEditText.doAfterTextChanged {
                onSearchQueryChanged(it?.toString().orEmpty())
            }

            buttonScan.setOnClickListener { presenter.onScanClicked() }
            imageViewScan.setOnClickListener { presenter.onScanClicked() }

            imageViewFieldButton.apply {
                isVisible = !requireContext().getClipboardText().isNullOrBlank()
                setOnClickListener { pasteText() }
            }
            buttonPaste.setOnClickListener { pasteText() }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.attachAdapter(searchAdapter)

            searchEditText.focusAndShowKeyboard()
        }

        presenter.loadInitialData()
    }

    private fun onSearchQueryChanged(newQuery: String) {
        presenter.search(newQuery)

        val isEmptyQuery = newQuery.isEmpty()

        with(binding) {
            val icon = if (isEmptyQuery) {
                imageViewFieldButton.isVisible = !requireContext().getClipboardText().isNullOrBlank()
                imageViewFieldButton.setOnClickListener { pasteText() }
                R.drawable.ic_search_paste
            } else {
                imageViewFieldButton.isVisible = true
                imageViewFieldButton.setOnClickListener { searchEditText.text.clear() }
                R.drawable.ic_close
            }

            binding.imageViewFieldButton.setImageResource(icon)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressBar.isInvisible = !isLoading
    }

    override fun showEmptyState(isEmpty: Boolean) {
        binding.groupEmptyView.isVisible = isEmpty
        binding.recyclerView.isVisible = !isEmpty
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
        showEmptyState(isEmpty = result.isEmpty())
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
        // TODO go to send
    }

    override fun showScanner() {
        val target = ScanQrFragment.create { onSearchQueryChanged(it) }
        addFragment(target)
    }

    private fun pasteText() = with(binding) {
        val nameOrAddress = requireContext().getClipboardText(trimmed = true)
        nameOrAddress?.let {
            searchEditText.apply {
                setText(it)
                setSelection(text.length)
            }
        }
    }
}
