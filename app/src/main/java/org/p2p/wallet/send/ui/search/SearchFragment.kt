package org.p2p.wallet.send.ui.search

import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.AnalyticsInteractor
import org.p2p.wallet.common.analytics.EventsName
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSearchBinding
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.Target
import org.p2p.wallet.send.ui.KEY_REQUEST_SEND
import org.p2p.wallet.send.ui.search.adapter.SearchAdapter
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.focusAndShowKeyboard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class SearchFragment :
    BaseMvpFragment<SearchContract.View, SearchContract.Presenter>(R.layout.fragment_search),
    SearchContract.View {

    companion object {
        const val EXTRA_RESULT = "EXTRA_RESULT"
        private const val EXTRA_USERNAMES = "EXTRA_USERNAMES"
        fun create(usernames: List<SearchResult>? = null) = SearchFragment()
            .withArgs(EXTRA_USERNAMES to usernames)
    }

    private val usernames: List<SearchResult>? by args(EXTRA_USERNAMES)
    override val presenter: SearchContract.Presenter by inject {
        parametersOf(usernames)
    }
    private val binding: FragmentSearchBinding by viewBinding()
    private val analyticsInteractor: AnalyticsInteractor by inject()
    private lateinit var textWatcher: TextWatcher

    private val searchAdapter: SearchAdapter by lazy {
        SearchAdapter {
            setFragmentResult(KEY_REQUEST_SEND, bundleOf(EXTRA_RESULT to it))
            parentFragmentManager.popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(EventsName.Send.RECIPIENT_ADDRESS)
        with(binding) {
            backImageView.setOnClickListener { popBackStack() }
            clearImageView.setOnClickListener { searchEditText.text.clear() }
            textWatcher = searchEditText.doAfterTextChanged {
                val value = it?.toString().orEmpty()
                val target = Target(value)
                presenter.search(target)

                clearImageView.isVisible = value.isNotEmpty()
            }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.attachAdapter(searchAdapter)

            searchEditText.focusAndShowKeyboard()
        }

        presenter.loadInitialData()
    }

    override fun showLoading(isLoading: Boolean) {
        with(binding) {
            progressBar.isInvisible = !isLoading
        }
    }

    override fun showSearchValue(value: String) {
        binding.searchEditText.removeTextChangedListener(textWatcher)
        binding.searchEditText.setText(value)
        binding.searchEditText.addTextChangedListener(textWatcher)
    }

    override fun showResult(result: List<SearchResult>) {
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
}