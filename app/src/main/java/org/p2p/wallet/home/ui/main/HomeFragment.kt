package org.p2p.wallet.home.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameOpenedFrom
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewSendEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.ActionButtonsView
import org.p2p.wallet.common.ui.widget.ActionButtonsViewClickListener
import org.p2p.wallet.databinding.FragmentHomeBinding
import org.p2p.wallet.databinding.LayoutHomeToolbarBinding
import org.p2p.wallet.debug.settings.DebugSettingsFragment
import org.p2p.wallet.deeplinks.CenterActionButtonClickSetter
import org.p2p.wallet.history.ui.token.TokenHistoryFragment
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.core.token.Token
import org.p2p.wallet.home.ui.main.adapter.TokenAdapter
import org.p2p.wallet.home.ui.main.bottomsheet.BuyInfoDetailsBottomSheet
import org.p2p.wallet.home.ui.main.bottomsheet.HomeAction
import org.p2p.wallet.home.ui.main.bottomsheet.HomeActionsBottomSheet
import org.p2p.wallet.home.ui.main.empty.EmptyViewAdapter
import org.p2p.wallet.home.ui.select.bottomsheet.SelectTokenBottomSheet
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.moonpay.ui.BuySolanaFragment
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.send.ui.main.SendFragment
import org.p2p.wallet.send.ui.search.NewSearchFragment
import org.p2p.wallet.settings.ui.settings.NewSettingsFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.core.utils.Constants
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.core.utils.formatUsd
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.getColor
import java.math.BigDecimal

private const val KEY_RESULT_TOKEN = "KEY_RESULT_TOKEN"
private const val KEY_REQUEST_TOKEN = "KEY_REQUEST_TOKEN"

private const val KEY_RESULT_ACTION = "KEY_RESULT_ACTION"
private const val KEY_REQUEST_ACTION = "KEY_REQUEST_ACTION"

private const val KEY_RESULT_TOKEN_INFO = "KEY_RESULT_TOKEN_INFO"
private const val KEY_REQUEST_TOKEN_INFO = "KEY_REQUEST_TOKEN_INFO"

class HomeFragment :
    BaseMvpFragment<HomeContract.View, HomeContract.Presenter>(R.layout.fragment_home),
    HomeContract.View {

    companion object {
        fun create(): HomeFragment = HomeFragment()
    }

    override val presenter: HomeContract.Presenter by inject()

    private val newSendEnabledFeatureToggle: NewSendEnabledFeatureToggle by inject()

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

        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST_TOKEN_INFO,
            viewLifecycleOwner,
            ::onFragmentResult
        )

        presenter.load()
    }

    override fun showAddressCopied(addressAndUsername: String) {
        requireContext().copyToClipBoard(addressAndUsername)
        showUiKitSnackBar(
            message = getString(R.string.home_address_snackbar_text),
            actionButtonResId = R.string.common_ok,
            actionBlock = Snackbar::dismiss
        )
    }

    override fun showBuyInfoScreen(token: Token) {
        BuyInfoDetailsBottomSheet.show(
            fm = childFragmentManager,
            token = token,
            requestKey = KEY_REQUEST_TOKEN_INFO,
            resultKey = KEY_RESULT_TOKEN_INFO
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
        viewActionButtons.isVisible = false

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

    private fun ActionButtonsView.setupActionButtons() {
        listener = ActionButtonsViewClickListener {
            when (it) {
                ActionButtonsView.ActionButton.BUY_BUTTON -> {
                    presenter.onBuyClicked()
                }
                ActionButtonsView.ActionButton.RECEIVE_BUTTON -> {
                    replaceFragment(ReceiveSolanaFragment.create(token = null))
                }
                ActionButtonsView.ActionButton.SEND_BUTTON -> {
                    if (newSendEnabledFeatureToggle.isFeatureEnabled) {
                        replaceFragment(NewSearchFragment.create())
                    } else {
                        replaceFragment(SendFragment.create())
                    }
                }
                ActionButtonsView.ActionButton.SWAP_BUTTON -> {
                    replaceFragment(OrcaSwapFragment.create())
                }
            }
        }
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            KEY_REQUEST_TOKEN -> {
                result.getParcelable<Token>(KEY_RESULT_TOKEN)?.let {
                    showOldBuyScreen(it)
                }
            }
            KEY_REQUEST_TOKEN_INFO -> {
                result.getParcelable<Token>(KEY_RESULT_TOKEN_INFO)?.let {
                    presenter.onInfoBuyTokenClicked(it)
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
            HomeAction.SWAP -> {
                replaceFragment(OrcaSwapFragment.create())
            }
            HomeAction.SEND -> {
                if (newSendEnabledFeatureToggle.isFeatureEnabled) {
                    replaceFragment(NewSearchFragment.create())
                } else {
                    replaceFragment(SendFragment.create())
                }
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
    }

    override fun showRefreshing(isRefreshing: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = isRefreshing
    }

    override fun showEmptyViewData(data: List<Any>) {
        emptyAdapter.setItems(data)
    }

    override fun showEmptyState(isEmpty: Boolean) {
        with(binding) {
            viewActionButtons.isVisible = !isEmpty
            viewBalance.root.isVisible = !isEmpty
            val updatedAdapter = if (isEmpty) emptyAdapter else contentAdapter
            if (homeRecyclerView.adapter != updatedAdapter) {
                homeRecyclerView.adapter = updatedAdapter
            }
            homeRecyclerView.setBackgroundColor(getColor(if (isEmpty) R.color.bg_smoke else R.color.bg_snow))
        }
    }

    override fun navigateToProfile() {
        replaceFragment(NewSettingsFragment.create())
    }

    override fun navigateToReserveUsername() {
        replaceFragment(ReserveUsernameFragment.create(from = ReserveUsernameOpenedFrom.SETTINGS))
    }

    override fun onBannerClicked(bannerId: Int) {
        when (bannerId) {
            R.id.home_banner_top_up -> {
                replaceFragment(ReceiveSolanaFragment.create(token = null))
            }
            R.string.home_username_banner_option -> {
                browseAnalytics.logBannerUsernamePressed()
                replaceFragment(ReserveUsernameFragment.create(from = ReserveUsernameOpenedFrom.SETTINGS))
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
        presenter.onBuyTokenClicked(token)
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
