package org.p2p.wallet.send.ui.search

import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.token.Token
import org.p2p.core.utils.hideKeyboard
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.bridge.send.SendFragmentFactory
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewSearchBinding
import org.p2p.wallet.send.analytics.NewSendAnalytics
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.ui.SearchOpenedFromScreen
import org.p2p.wallet.send.ui.search.adapter.SearchAdapter
import org.p2p.wallet.qr.ui.ScanQrFragment
import org.p2p.wallet.svl.analytics.SendViaLinkAnalytics
import org.p2p.wallet.svl.model.SvlWidgetState
import org.p2p.wallet.svl.ui.send.SendViaLinkFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextResOrGone

private const val REQUEST_QR_KEY = "REQUEST_QR_KEY"
private const val RESULT_QR_KEY = "RESULT_QR_KEY"

private const val ARG_TOKEN = "EXTRA_TOKEN"
private const val ARG_OPENED_FROM = "ARG_OPENED_FROM"

class NewSearchFragment :
    BaseMvpFragment<NewSearchContract.View, NewSearchContract.Presenter>(R.layout.fragment_new_search),
    NewSearchContract.View {

    companion object {
        fun create(openedFromScreen: SearchOpenedFromScreen): NewSearchFragment =
            NewSearchFragment()
                .withArgs(ARG_OPENED_FROM to openedFromScreen)

        fun create(
            selectedToken: Token.Active,
            openedFromScreen: SearchOpenedFromScreen
        ): NewSearchFragment =
            NewSearchFragment()
                .withArgs(
                    ARG_TOKEN to selectedToken,
                    ARG_OPENED_FROM to openedFromScreen
                )
    }

    private val selectedToken: Token.Active? by args(ARG_TOKEN)
    private val openedFromScreen: SearchOpenedFromScreen by args(ARG_OPENED_FROM)

    override val presenter: NewSearchContract.Presenter by inject { parametersOf(selectedToken) }
    private val binding: FragmentNewSearchBinding by viewBinding()

    private val sendFragmentFactory: SendFragmentFactory by inject()
    private val newSendAnalytics: NewSendAnalytics by inject()
    private val svlAnalytics: SendViaLinkAnalytics by inject()

    private val searchAdapter: SearchAdapter by unsafeLazy {
        SearchAdapter(
            onItemClicked = presenter::onSearchResultClick,
            usernameDomainFeatureToggle = get()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newSendAnalytics.logSearchScreenOpened(openedFromScreen)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStack()
        }

        setOnResultListener()

        with(binding) {
            binding.toolbar.apply {
                setSearchMenu(searchHintRes = R.string.search_edittext_hint)
                setNavigationOnClickListener { popBackStack() }
                setOnDoneListener { hideKeyboard() }
                onQueryUpdated = presenter::search
            }

            buttonScanQr.setOnClickListener { presenter.onScanClicked() }

            widgetSvl.setOnClickListener {
                svlAnalytics.logStartCreateLink()
                replaceFragment(SendViaLinkFragment.create(initialToken = selectedToken))
            }

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

    override fun showLoading(isLoading: Boolean) {
        binding.progressBar.isInvisible = !isLoading
    }

    override fun showSendViaLink(isVisible: Boolean) {
        binding.widgetSvl.isVisible = isVisible
    }

    override fun updateLinkWidgetState(state: SvlWidgetState) {
        binding.widgetSvl.updateState(state)
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
        binding.messageTextView.withTextResOrGone(textRes)
    }

    override fun submitSearchResult(searchResult: SearchResult, initialToken: Token.Active?) {
        replaceFragment(sendFragmentFactory.sendFragment(searchResult, initialToken))
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
