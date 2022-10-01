package org.p2p.wallet.home.ui.main

import androidx.core.view.isVisible
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ReserveMode
import org.p2p.wallet.auth.ui.username.ReserveUsernameFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentHomeBinding
import org.p2p.wallet.databinding.LayoutActionButtonsBinding
import org.p2p.wallet.databinding.LayoutHomeToolbarBinding
import org.p2p.wallet.debug.settings.DebugSettingsFragment
import org.p2p.wallet.deeplinks.CenterActionButtonClickSetter
import org.p2p.wallet.history.ui.token.TokenHistoryFragment
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.ui.main.adapter.TokenAdapter
import org.p2p.wallet.home.ui.main.bottomsheet.HomeAction
import org.p2p.wallet.home.ui.main.bottomsheet.HomeActionsBottomSheet
import org.p2p.wallet.home.ui.main.empty.EmptyViewAdapter
import org.p2p.wallet.home.ui.select.bottomsheet.SelectTokenBottomSheet
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.moonpay.ui.BuySolanaFragment
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.receive.token.ReceiveTokenFragment
import org.p2p.wallet.send.ui.main.SendFragment
import org.p2p.wallet.settings.ui.settings.NewSettingsFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.Constants
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
    private val receiveAnalytics: ReceiveAnalytics by inject()

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
            HomeActionsBottomSheet.show(
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

    override fun showAddressCopied(address: String) {
        requireContext().copyToClipBoard(address)
        showUiKitSnackBar(
            message = getString(R.string.home_address_snackbar_text),
            actionButtonResId = R.string.common_ok,
            actionBlock = Snackbar::dismiss
        )
    }

    private fun FragmentHomeBinding.setupView() {
        layoutToolbar.setupToolbar()

        homeRecyclerView.adapter = contentAdapter

        viewActionButtons.setupActionButtons()

        swipeRefreshLayout.setOnRefreshListener {
            presenter.refreshTokens()
        }

        // hidden. temporary. PWN-4381
        viewBuyTokenBanner.root.isVisible = false

        if (BuildConfig.DEBUG) {
            with(layoutToolbar) {
                viewDebugShadow.isVisible = true
                imageViewDebug.isVisible = true
                imageViewDebug.setOnClickListener {
                    replaceFragment(DebugSettingsFragment.create())
                }
            }
        }
    }

    private fun LayoutHomeToolbarBinding.setupToolbar() {
        textViewAddress.setOnClickListener {
            receiveAnalytics.logAddressOnMainClicked()
            presenter.onAddressClicked()
        }
        imageViewProfile.setOnClickListener { presenter.onProfileClick() }
        imageViewQr.setOnClickListener { replaceFragment(ReceiveSolanaFragment.create(token = null)) }
    }

    private fun LayoutActionButtonsBinding.setupActionButtons() {
        viewActionBuy.apply {
            textViewButtonTitle.setText(R.string.home_buy)
            imageButtonButtonIcon.setImageResource(R.drawable.ic_plus)
            imageButtonButtonIcon.setOnClickListener {
                presenter.onBuyClicked()
            }
        }
        viewActionReceive.apply {
            textViewButtonTitle.setText(R.string.home_receive)
            imageButtonButtonIcon.setImageResource(R.drawable.ic_receive_simple)
            imageButtonButtonIcon.setOnClickListener {
                replaceFragment(ReceiveSolanaFragment.create(token = null))
            }
        }
        viewActionSend.apply {
            textViewButtonTitle.setText(R.string.home_send)
            imageButtonButtonIcon.setImageResource(R.drawable.ic_send_medium)
            imageButtonButtonIcon.setOnClickListener {
                replaceFragment(SendFragment.create())
            }
        }
        viewActionTrade.apply {
            textViewButtonTitle.setText(R.string.home_trade)
            imageButtonButtonIcon.setImageResource(R.drawable.ic_swap_medium)
            imageButtonButtonIcon.setOnClickListener {
                replaceFragment(OrcaSwapFragment.create())
            }
        }
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            KEY_REQUEST_TOKEN -> {
                result.getParcelable<Token>(KEY_RESULT_TOKEN)?.let {
                    this.showOldBuyScreen(it)
                }
            }
            KEY_REQUEST_ACTION -> {
                (result.getSerializable(KEY_RESULT_ACTION) as? HomeAction)?.let {
                    openScreenByHomeAction(it)
                }
            }
        }
    }

    private fun openScreenByHomeAction(action: HomeAction) {
        when (action) {
            HomeAction.BUY -> {
                presenter.onBuyClicked()
            }
            HomeAction.RECEIVE -> {
                replaceFragment(ReceiveSolanaFragment.create(token = null))
            }
            HomeAction.TRADE -> {
                replaceFragment(OrcaSwapFragment.create())
            }
            HomeAction.SEND -> {
                replaceFragment(SendFragment.create())
            }
        }
    }

    override fun showOldBuyScreen(token: Token) {
        replaceFragment(BuySolanaFragment.create(token))
    }

    override fun showNewBuyScreen(token: Token) {
        replaceFragment(NewBuyFragment.create(token))
    }

    override fun showUserAddress(ellipsizedAddress: String) {
        binding.layoutToolbar.textViewAddress.text = ellipsizedAddress
    }

    override fun showTokens(tokens: List<HomeElementItem>, isZerosHidden: Boolean) {
        contentAdapter.setItems(tokens, isZerosHidden)
    }

    override fun showTokensForBuy(tokens: List<Token>, newBuyEnabled: Boolean) {
        if (newBuyEnabled) {
            tokens.find { it.tokenSymbol == Constants.USDC_SYMBOL }?.let { token ->
                replaceFragment(NewBuyFragment.create(token))
            }
        } else {
            SelectTokenBottomSheet.show(
                fm = childFragmentManager,
                tokens = tokens,
                requestKey = KEY_REQUEST_TOKEN,
                resultKey = KEY_RESULT_TOKEN
            )
        }
    }

    override fun showBalance(balance: BigDecimal) {
        binding.viewBalance.textViewAmount.text = getString(R.string.home_usd_format, balance.formatUsd())
        binding.viewBalance.textViewTitle.setText(R.string.home_balance_title)
    }

    override fun showRefreshing(isRefreshing: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = isRefreshing
    }

    override fun showEmptyViewData(data: List<Any>) {
        emptyAdapter.setItems(data)
    }

    override fun showEmptyState(isEmpty: Boolean) {
        with(binding) {
            viewActionButtons.root.isVisible = !isEmpty
            viewBalance.root.isVisible = !isEmpty
            val updatedAdapter = if (isEmpty) emptyAdapter else contentAdapter
            if (homeRecyclerView.adapter != updatedAdapter) homeRecyclerView.adapter = updatedAdapter
        }
    }

    override fun navigateToProfile() {
        replaceFragment(NewSettingsFragment.create())
    }

    override fun navigateToReserveUsername() {
        replaceFragment(ReserveUsernameFragment.create(mode = ReserveMode.POP, isSkipStepEnabled = false))
    }

    override fun onBannerClicked(bannerId: Int) {
        when (bannerId) {
            R.id.home_banner_top_up -> {
                presenter.onBuyClicked()
            }
            R.string.home_username_banner_option -> {
                browseAnalytics.logBannerUsernamePressed()
                replaceFragment(ReserveUsernameFragment.create(ReserveMode.POP, isSkipStepEnabled = false))
            }
            R.string.home_feedback_banner_option -> {
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
            if (token is Token.Active) {
                replaceFragment(ReceiveTokenFragment.create(token))
            } else {
                openScreenByHomeAction(HomeAction.RECEIVE)
            }
        } else {
            presenter.onBuyTokenClicked(token)
        }
    }

    override fun onHideClicked(token: Token.Active) {
        presenter.toggleTokenVisibility(token)
    }

    override fun onDestroy() {
        /* We are clearing cache only if activity is destroyed */
        presenter.clearTokensCache()
        super.onDestroy()
    }

    fun updateTokensIfNeeded() {
        presenter.updateTokensIfNeeded()
    }
}
