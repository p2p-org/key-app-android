package org.p2p.wallet.home.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.p2p.uikit.natives.showSnackbarShort
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ReserveMode
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.ActionButtonsView
import org.p2p.wallet.databinding.FragmentHomeBinding
import org.p2p.wallet.databinding.LayoutHomeToolbarBinding
import org.p2p.wallet.debug.settings.DebugSettingsFragment
import org.p2p.wallet.deeplinks.CenterActionButtonClickSetter
import org.p2p.wallet.history.ui.token.TokenHistoryFragment
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.home.ui.main.adapter.TokenAdapter
import org.p2p.wallet.home.ui.main.bottomsheet.MainAction
import org.p2p.wallet.home.ui.main.bottomsheet.MainActionsBottomSheet
import org.p2p.wallet.home.ui.main.empty.EmptyViewAdapter
import org.p2p.wallet.home.ui.select.bottomsheet.SelectTokenBottomSheet
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.moonpay.ui.BuySolanaFragment
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.receive.token.ReceiveTokenFragment
import org.p2p.wallet.send.ui.main.SendFragment
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.formatUsd
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.unsafeLazy
import java.math.BigDecimal

private const val KEY_RESULT_TOKEN = "KEY_RESULT_TOKEN"
private const val KEY_REQUEST_TOKEN = "KEY_REQUEST_TOKEN"

private const val KEY_RESULT_ACTION = "KEY_RESULT_ACTION"
private const val KEY_REQUEST_ACTION = "KEY_REQUEST_ACTION"

class HomeFragment :
    BaseMvpFragment<HomeContract.View, HomeContract.Presenter>(R.layout.fragment_home),
    HomeContract.View {

    companion object {
        fun create(): HomeFragment = HomeFragment()
    }

    override val presenter: HomeContract.Presenter by inject()

    private lateinit var binding: FragmentHomeBinding

    private val contentAdapter: TokenAdapter by unsafeLazy {
        TokenAdapter(this)
    }

    private val emptyAdapter: EmptyViewAdapter by unsafeLazy {
        EmptyViewAdapter(this)
    }

    private val browseAnalytics: BrowseAnalytics by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setupView()

        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST_TOKEN,
            viewLifecycleOwner,
            ::onFragmentResult
        )

        presenter.subscribeToUserTokensFlow()
        val centerActionSetter = parentFragment as? CenterActionButtonClickSetter

        centerActionSetter?.setOnCenterActionButtonListener {
            MainActionsBottomSheet.show(
                fm = childFragmentManager,
                requestKey = KEY_REQUEST_ACTION,
                resultKey = KEY_RESULT_ACTION
            )
        }

        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST_ACTION,
            viewLifecycleOwner,
            ::onFragmentResult
        )
    }

    private fun FragmentHomeBinding.setupView() {
        val commonTitle = getString(R.string.app_name)
        val beta = getString(R.string.common_beta)
        val color = getColor(R.color.textIconSecondary)

        layoutToolbar.setupToolbar()

        mainRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        mainRecyclerView.adapter = contentAdapter

        actionButtonsView.setupActionButtons()

        swipeRefreshLayout.setOnRefreshListener {
            presenter.refreshTokens()
        }

        // hidden. temporary. PWN-4381
        viewBuyTokenBanner.root.isVisible = false

        if (BuildConfig.DEBUG) {
            with(layoutToolbar.imageViewDebug) {
                isVisible = true
                setOnClickListener {
                    replaceFragment(DebugSettingsFragment.create())
                }
            }
        }
    }

    private fun LayoutHomeToolbarBinding.setupToolbar() {
        textViewAddress.setOnClickListener {
            val address = textViewAddress.text
            requireContext().copyToClipBoard(address.toString())
            val snackbar = binding.root.showSnackbarShort(
                getString(R.string.main_address_snackbar_text),
                getString(R.string.common_ok)
            ) { it.dismiss() }
        }

        imageViewProfile.setOnClickListener { replaceFragment(SettingsFragment.create()) }
        imageViewQr.setOnClickListener { replaceFragment(ReceiveSolanaFragment.create(token = null)) }
    }

    private fun ActionButtonsView.setupActionButtons() {
        onBuyItemClickListener = {
            presenter.onBuyClicked()
        }
        onReceiveItemClickListener = {
            replaceFragment(ReceiveSolanaFragment.create(token = null))
        }
        onSendClickListener = {
            replaceFragment(SendFragment.create())
        }
        onSwapItemClickListener = {
            replaceFragment(OrcaSwapFragment.create())
        }
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            KEY_REQUEST_TOKEN -> {
                result.getParcelable<Token>(KEY_RESULT_TOKEN)?.let {
                    showBuyTokenScreen(it)
                }
            }
            KEY_REQUEST_ACTION -> {
                (result.getSerializable(KEY_RESULT_ACTION) as? MainAction)?.let {
                    openScreenByMainAction(it)
                }
            }
        }
    }

    private fun openScreenByMainAction(action: MainAction) {
        when (action) {
            MainAction.BUY -> {
                presenter.onBuyClicked()
            }
            MainAction.RECEIVE -> {
                replaceFragment(ReceiveSolanaFragment.create(token = null))
            }
            MainAction.TRADE -> {
                replaceFragment(OrcaSwapFragment.create())
            }
            MainAction.SEND -> {
                replaceFragment(SendFragment.create())
            }
        }
    }

    private fun showBuyTokenScreen(token: Token) {
        replaceFragment(BuySolanaFragment.create(token))
    }

    override fun showUserAddress(ellipsizedAddress: String) {
        binding.layoutToolbar.textViewAddress.text = ellipsizedAddress
    }

    override fun showTokens(tokens: List<HomeElementItem>, isZerosHidden: Boolean, state: VisibilityState) {
        contentAdapter.setItems(tokens, isZerosHidden, state)
    }

    override fun showTokensForBuy(tokens: List<Token>) {
        SelectTokenBottomSheet.show(
            fm = childFragmentManager,
            tokens = tokens,
            requestKey = KEY_REQUEST_TOKEN,
            resultKey = KEY_RESULT_TOKEN
        )
    }

    override fun showBalance(balance: BigDecimal, username: Username?) = with(binding.layoutBalance) {
        textViewAmount.text = getString(R.string.main_usd_format, balance.formatUsd())
        if (username == null) {
            textViewTitle.setText(R.string.main_balance)
        } else {
            val commonText = username.getFullUsername(requireContext())
            val color = getColor(R.color.textIconPrimary)
            textViewTitle.text = SpanUtils.highlightText(commonText, username.username, color)
        }
    }

    override fun showRefreshing(isRefreshing: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = isRefreshing
    }

    override fun showEmptyViewData(data: List<Any>) {
        emptyAdapter.setItems(data)
    }

    override fun showActions(items: List<ActionButtonsView.ActionButton>) {
        binding.actionButtonsView.setItems(items)
    }

    override fun showEmptyState(isEmpty: Boolean) {
        with(binding) {
            actionButtonsView.isVisible = !isEmpty
            layoutBalance.root.isVisible = !isEmpty
            mainRecyclerView.adapter = if (isEmpty) emptyAdapter else contentAdapter
        }
    }

    override fun onDestroy() {
        /* We are clearing cache only if activity is destroyed */
        presenter.clearTokensCache()
        super.onDestroy()
    }

    override fun onBannerClicked(bannerId: Int) {
        when (bannerId) {
            R.id.home_banner_top_up -> {
                presenter.onBuyClicked()
            }
            R.string.main_username_banner_option -> {
                browseAnalytics.logBannerUsernamePressed()
                replaceFragment(ReserveUsernameFragment.create(ReserveMode.POP, isSkipStepEnabled = false))
            }
            R.string.main_feedback_banner_option -> {
                browseAnalytics.logBannerFeedbackPressed()
                IntercomService.showMessenger()
            }
        }
    }

    override fun onToggleClicked() {
        presenter.toggleTokenVisibilityState()
    }

    override fun onTokenClicked(token: Token.Active) {
        replaceFragment(TokenHistoryFragment.create(token))
    }

    override fun onPopularTokenClicked(token: Token) {
        if (token.isRenBTC) {
            replaceFragment(ReceiveTokenFragment.create(token as Token.Active))
        } else {
            showBuyTokenScreen(token)
        }
    }

    override fun onHideClicked(token: Token.Active) {
        presenter.toggleTokenVisibility(token)
    }
}
