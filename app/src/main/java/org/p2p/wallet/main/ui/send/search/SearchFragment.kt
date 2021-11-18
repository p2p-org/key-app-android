package org.p2p.wallet.main.ui.send.search

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSearchBinding
import org.p2p.wallet.main.model.SearchResult
import org.p2p.wallet.main.model.Target
import org.p2p.wallet.main.ui.send.SendFragment.Companion.KEY_REQUEST_SEND
import org.p2p.wallet.main.ui.send.search.adapter.SearchAdapter
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class SearchFragment :
    BaseMvpFragment<SearchContract.View, SearchContract.Presenter>(R.layout.fragment_search),
    SearchContract.View {

    companion object {
        const val EXTRA_RESULT = "EXTRA_RESULT"

        fun create() = SearchFragment()
    }

    override val presenter: SearchContract.Presenter by inject()

    private val binding: FragmentSearchBinding by viewBinding()

    private val searchAdapter: SearchAdapter by lazy {
        SearchAdapter {
            setFragmentResult(KEY_REQUEST_SEND, bundleOf(EXTRA_RESULT to it))
            parentFragmentManager.popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            backImageView.setOnClickListener { popBackStack() }
            clearImageView.setOnClickListener { searchEditText.text.clear() }
            searchEditText.doAfterTextChanged {
                val value = it?.toString().orEmpty().trim()
                val target = Target(value)
                presenter.search(target)

                clearImageView.isVisible = value.isNotEmpty()
            }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.attachAdapter(searchAdapter)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        with(binding) {
            progressBar.isInvisible = !isLoading
        }
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