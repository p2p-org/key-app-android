package org.p2p.wallet.newsend.ui.search

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
import org.p2p.core.token.Token
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewSearchBinding
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.newsend.ui.NewSendFragment
import org.p2p.wallet.qr.ui.ScanQrFragment
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.ui.search.adapter.SearchAdapter
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextResOrGone

private const val REQUEST_QR_KEY = "REQUEST_QR_KEY"
private const val RESULT_QR_KEY = "RESULT_QR_KEY"

private const val EXTRA_USERNAMES = "EXTRA_USERNAMES"
private const val EXTRA_TOKEN = "EXTRA_TOKEN"

class NewSearchFragment :
    BaseMvpFragment<NewSearchContract.View, NewSearchContract.Presenter>(R.layout.fragment_new_search),
    NewSearchContract.View {

    companion object {
        fun create(preselectedRecipients: List<SearchResult>? = null): NewSearchFragment =
            NewSearchFragment()
                .withArgs(EXTRA_USERNAMES to preselectedRecipients)

        fun create(selectedToken: Token.Active): NewSearchFragment =
            NewSearchFragment()
                .withArgs(EXTRA_TOKEN to selectedToken)
    }

    private val usernames: List<SearchResult>? by args(EXTRA_USERNAMES)
    private val selectedToken: Token.Active? by args(EXTRA_TOKEN)

    override val presenter: NewSearchContract.Presenter by inject {
        parametersOf(usernames, selectedToken)
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

    private var lastQuery: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }

        setOnResultListener()

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
                    searchHintRes = R.string.search_edittext_hint,
                    lastQuery = lastQuery
                )
                setNavigationOnClickListener { popBackStack() }
            }

            buttonContinue.setOnClickListener { presenter.onContinueClicked() }

            buttonBuy.setOnClickListener {
                presenter.onBuyClicked()
            }
            buttonScanQr.setOnClickListener {
                presenter.onScanClicked()
            }
            buttonReceive.setOnClickListener {
                replaceFragment(ReceiveSolanaFragment.create(token = null))
            }

            recyclerViewSearchResults.apply {
                itemAnimator = null
                layoutManager = LinearLayoutManager(requireContext())
                attachAdapter(searchAdapter)
            }
        }

        presenter.loadInitialData()
    }

    private fun onSearchQueryChanged(newQuery: String) {
        lastQuery = newQuery
        presenter.search(newQuery)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressBar.isInvisible = !isLoading
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

    override fun setListBackgroundVisibility(isVisible: Boolean) {
        binding.recyclerViewSearchResults.apply {
            if (isVisible) {
                setBackgroundResource(R.drawable.bg_snow_rounded_16)
            } else {
                background = null
            }
        }
    }

    override fun setContinueButtonVisibility(isVisible: Boolean) {
        binding.buttonContinue.isVisible = isVisible
    }

    override fun setBuyReceiveButtonsVisibility(isVisible: Boolean) {
        binding.groupReceiveBuyButtons.isVisible = isVisible
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

    override fun submitSearchResult(searchResult: SearchResult, initialToken: Token.Active?) {
        replaceFragment(NewSendFragment.create(recipient = searchResult, initialToken = initialToken))
    }

    override fun showScanner() {
        replaceFragment(ScanQrFragment.create(REQUEST_QR_KEY, RESULT_QR_KEY))
    }

    override fun showBuyScreen(token: Token) {
        replaceFragment(NewBuyFragment.create(token = token))
    }

    private fun setOnResultListener() {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            REQUEST_QR_KEY, viewLifecycleOwner
        ) { _, bundle ->
            bundle.getString(RESULT_QR_KEY)?.let { address ->
                showSearchValue(address)
            }
        }
    }
}
