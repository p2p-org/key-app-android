package org.p2p.wallet.home.ui.main

import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.p2p.core.glide.GlideManager
import org.p2p.core.token.Token
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bindOrGone
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameOpenedFrom
import org.p2p.wallet.bridge.analytics.ClaimAnalytics
import org.p2p.wallet.bridge.claim.ui.ClaimFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.permissions.PermissionState
import org.p2p.wallet.common.permissions.new.requestPermissionNotification
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.databinding.FragmentHomeBinding
import org.p2p.wallet.databinding.LayoutHomeToolbarBinding
import org.p2p.wallet.debug.settings.DebugSettingsFragment
import org.p2p.wallet.deeplinks.CenterActionButtonClickSetter
import org.p2p.wallet.history.ui.token.TokenHistoryFragment
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.ui.main.adapter.TokenAdapter
import org.p2p.wallet.home.ui.main.bottomsheet.BuyInfoDetailsBottomSheet
import org.p2p.wallet.home.ui.main.bottomsheet.HomeAction
import org.p2p.wallet.home.ui.main.bottomsheet.HomeActionsBottomSheet
import org.p2p.wallet.home.ui.main.empty.EmptyViewAdapter
import org.p2p.wallet.home.ui.select.bottomsheet.SelectTokenBottomSheet
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.moonpay.ui.BuySolanaFragment
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.newsend.ui.SearchOpenedFromScreen
import org.p2p.wallet.newsend.ui.search.NewSearchFragment
import org.p2p.wallet.newsend.ui.stub.SendUnavailableFragment
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.push_notifications.analytics.AnalyticsPushChannel
import org.p2p.wallet.receive.ReceiveFragmentFactory
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.sell.ui.payload.SellPayloadFragment
import org.p2p.wallet.settings.ui.settings.NewSettingsFragment
import org.p2p.wallet.swap.ui.SwapFragmentFactory
import org.p2p.wallet.swap.ui.orca.SwapOpenedFrom
import org.p2p.wallet.utils.HomeScreenLayoutManager
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.viewBinding

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

    private val binding: FragmentHomeBinding by viewBinding()

    private val glideManager: GlideManager by inject()
    private val analytics: AnalyticsPushChannel by inject()

    private val contentAdapter: TokenAdapter by unsafeLazy {
        TokenAdapter(
            glideManager = glideManager,
            listener = this
        )
    }

    private val emptyAdapter: EmptyViewAdapter by unsafeLazy { EmptyViewAdapter(this) }

    private val browseAnalytics: BrowseAnalytics by inject()
    private val receiveAnalytics: ReceiveAnalytics by inject()
    private val claimAnalytics: ClaimAnalytics by inject()

    private val swapFragmentFactory: SwapFragmentFactory by inject()
    private val receiveFragmentFactory: ReceiveFragmentFactory by inject()

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
        lifecycle.addObserver(presenter)

        presenter.load()

        requestPermissionNotification { permissionState ->
            if (permissionState == PermissionState.GRANTED) {
                analytics.pushPermissionsAllowed()
                AppNotificationManager.createNotificationChannels(requireContext())
            }
        }
    }

    override fun showAddressCopied(addressOrUsername: String) {
        requireContext().copyToClipBoard(addressOrUsername)
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

    override fun showActionButtons(buttons: List<ActionButton>) {
        binding.viewActionButtons.showActionButtons(buttons)
    }

    private fun FragmentHomeBinding.setupView() {
        layoutToolbar.setupToolbar()

        homeRecyclerView.adapter = contentAdapter
        homeRecyclerView.layoutManager = HomeScreenLayoutManager(requireContext())
        swipeRefreshLayout.setOnRefreshListener { presenter.refreshTokens() }
        viewActionButtons.onButtonClicked = { onActionButtonClicked(it) }

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

    private fun onActionButtonClicked(clickedButton: ActionButton) {
        when (clickedButton) {
            ActionButton.BUY_BUTTON -> {
                presenter.onBuyClicked()
            }
            ActionButton.RECEIVE_BUTTON -> {
                replaceFragment(receiveFragmentFactory.receiveFragment(token = null))
            }
            ActionButton.SEND_BUTTON -> {
                presenter.onSendClicked(clickSource = SearchOpenedFromScreen.MAIN)
            }
            ActionButton.SELL_BUTTON -> {
                replaceFragment(SellPayloadFragment.create())
            }
            ActionButton.SWAP_BUTTON -> {
                replaceFragment(swapFragmentFactory.swapFragment(source = SwapOpenedFrom.MAIN_SCREEN))
            }
        }
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            KEY_REQUEST_TOKEN -> {
                result.getParcelable<Token>(KEY_RESULT_TOKEN)?.also(::showOldBuyScreen)
            }
            KEY_REQUEST_TOKEN_INFO -> {
                result.getParcelable<Token>(KEY_RESULT_TOKEN_INFO)?.also(presenter::onInfoBuyTokenClicked)
            }
            KEY_REQUEST_ACTION -> {
                (result.getSerializable(KEY_RESULT_ACTION) as? HomeAction)?.also(::openScreenByHomeAction)
            }
        }
    }

    private fun openScreenByHomeAction(action: HomeAction) {
        when (action) {
            HomeAction.SELL -> replaceFragment(SellPayloadFragment.create())
            HomeAction.BUY -> presenter.onBuyClicked()
            HomeAction.RECEIVE -> replaceFragment(receiveFragmentFactory.receiveFragment(token = null))
            HomeAction.SWAP -> replaceFragment(swapFragmentFactory.swapFragment(source = SwapOpenedFrom.ACTION_PANEL))
            HomeAction.SEND -> presenter.onSendClicked(clickSource = SearchOpenedFromScreen.ACTION_PANEL)
        }
    }

    override fun showNewSendScreen(openedFromScreen: SearchOpenedFromScreen) {
        replaceFragment(NewSearchFragment.create(openedFromScreen))
    }

    override fun showOldBuyScreen(token: Token) {
        replaceFragment(BuySolanaFragment.create(token))
    }

    override fun showSendNoTokens(fallbackToken: Token) {
        replaceFragment(SendUnavailableFragment.create(fallbackToken))
    }

    override fun showNewBuyScreen(token: Token) {
        replaceFragment(NewBuyFragment.create(token))
    }

    override fun showUserAddress(ellipsizedAddress: String) {
        binding.layoutToolbar.textViewAddress.text = ellipsizedAddress
    }

    override fun showTokens(tokens: List<HomeElementItem>, isZerosHidden: Boolean) {
        binding.homeRecyclerView.post {
            contentAdapter.setItems(tokens, isZerosHidden)
        }
    }

    override fun showTokensForBuy(tokens: List<Token>) {
        SelectTokenBottomSheet.show(
            fm = childFragmentManager,
            tokens = tokens,
            requestKey = KEY_REQUEST_TOKEN,
            resultKey = KEY_RESULT_TOKEN
        )
    }

    override fun showBalance(cellModel: TextViewCellModel?) {
        binding.viewBalance.textViewAmount.bindOrGone(cellModel)
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
            viewBalance.textViewAmount.isVisible = !isEmpty

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
                replaceFragment(receiveFragmentFactory.receiveFragment(token = null))
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

    override fun onClaimTokenClicked(token: Token.Eth) {
        claimAnalytics.logClaimButtonClicked()
        replaceFragment(
            ClaimFragment.create(ethereumToken = token)
        )
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
