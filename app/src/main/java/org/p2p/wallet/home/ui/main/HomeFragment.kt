package org.p2p.wallet.home.ui.main

import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Context
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import timber.log.Timber
import org.p2p.core.crypto.Base58String
import org.p2p.core.glide.GlideManager
import org.p2p.core.token.Token
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bindOrGone
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameOpenedFrom
import org.p2p.wallet.bridge.claim.ui.ClaimFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.permissions.PermissionState
import org.p2p.wallet.common.permissions.new.requestPermissionNotification
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.databinding.FragmentHomeBinding
import org.p2p.wallet.databinding.LayoutHomeToolbarBinding
import org.p2p.wallet.debug.settings.DebugSettingsFragment
import org.p2p.wallet.history.ui.token.TokenHistoryFragment
import org.p2p.wallet.home.analytics.HomeAnalytics
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.ui.main.adapter.TokenAdapter
import org.p2p.wallet.home.ui.main.bottomsheet.BuyInfoDetailsBottomSheet
import org.p2p.wallet.home.ui.main.empty.EmptyViewAdapter
import org.p2p.wallet.home.ui.select.bottomsheet.SelectTokenBottomSheet
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.jupiter.ui.main.JupiterSwapFragment
import org.p2p.wallet.kyc.StrigaFragmentFactory
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.moonpay.ui.BuyFragmentFactory
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.newsend.ui.SearchOpenedFromScreen
import org.p2p.wallet.newsend.ui.search.NewSearchFragment
import org.p2p.wallet.newsend.ui.stub.SendUnavailableFragment
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.push_notifications.analytics.AnalyticsPushChannel
import org.p2p.wallet.receive.ReceiveFragmentFactory
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.sell.ui.payload.SellPayloadFragment
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.striga.iban.StrigaUserIbanDetailsFragment
import org.p2p.wallet.striga.kyc.ui.StrigaKycPendingBottomSheet
import org.p2p.wallet.striga.sms.onramp.StrigaOnRampSmsInputFragment
import org.p2p.wallet.striga.ui.TopUpWalletBottomSheet
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.utils.HomeScreenLayoutManager
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.getParcelableCompat
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.replaceFragmentForResult
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.viewBinding

private const val KEY_RESULT_TOKEN = "KEY_RESULT_TOKEN"
private const val KEY_REQUEST_TOKEN = "KEY_REQUEST_TOKEN"

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

    private var listener: RootListener? = null

    private val contentAdapter: TokenAdapter by unsafeLazy {
        TokenAdapter(
            glideManager = glideManager,
            listener = this
        )
    }

    private val emptyAdapter: EmptyViewAdapter by unsafeLazy { EmptyViewAdapter(this) }

    private val receiveAnalytics: ReceiveAnalytics by inject()
    private val homeAnalytics: HomeAnalytics by inject()
    private val buyAnalytics: BuyAnalytics by inject()

    private val receiveFragmentFactory: ReceiveFragmentFactory by inject()
    private val strigaKycFragmentFactory: StrigaFragmentFactory by inject()
    private val buyFragmentFactory: BuyFragmentFactory by inject()

    private lateinit var layoutManager: LinearLayoutManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? RootListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setupView()

        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST_TOKEN,
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

    override fun showSwapWithArgs(tokenASymbol: String, tokenBSymbol: String, amountA: String, source: SwapOpenedFrom) {
        replaceFragment(
            JupiterSwapFragment.create(tokenASymbol, tokenBSymbol, amountA, source)
        )
    }

    override fun showSwap(source: SwapOpenedFrom) {
        replaceFragment(JupiterSwapFragment.create(source = source))
    }

    override fun showCashOut() {
        replaceFragment(SellPayloadFragment.create())
    }

    override fun showTopup() {
        TopUpWalletBottomSheet.show(fm = parentFragmentManager)
    }

    override fun showSwap() {
        showSwap(source = SwapOpenedFrom.MAIN_SCREEN)
    }

    private fun FragmentHomeBinding.setupView() {
        layoutManager = HomeScreenLayoutManager(requireContext())
        layoutToolbar.setupToolbar()

        homeRecyclerView.adapter = contentAdapter
        homeRecyclerView.doOnAttach {
            homeRecyclerView.layoutManager = layoutManager
        }
        homeRecyclerView.doOnDetach {
            homeRecyclerView.layoutManager = null
        }
        swipeRefreshLayout.setOnRefreshListener(presenter::refreshTokens)
        viewActionButtons.onButtonClicked = ::onActionButtonClicked

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
            ActionButton.SEND_BUTTON -> {
                presenter.onSendClicked(clickSource = SearchOpenedFromScreen.MAIN)
            }
            ActionButton.SELL_BUTTON -> {
                presenter.onSellClicked()
            }
            ActionButton.SWAP_BUTTON -> {
                presenter.onSwapClicked()
            }
            ActionButton.TOP_UP_BUTTON -> {
                presenter.onTopupClicked()
            }
            else -> {
                // unsupported on this screen
            }
        }
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            KEY_REQUEST_TOKEN -> {
                result.getParcelableCompat<Token>(KEY_RESULT_TOKEN)?.also(::navigateToBuyScreen)
            }
            KEY_REQUEST_TOKEN_INFO -> {
                result.getParcelableCompat<Token>(KEY_RESULT_TOKEN_INFO)?.also(presenter::onInfoBuyTokenClicked)
            }
        }
    }

    override fun showNewSendScreen(openedFromScreen: SearchOpenedFromScreen) {
        replaceFragment(NewSearchFragment.create(openedFromScreen))
    }

    override fun navigateToBuyScreen(token: Token) {
        replaceFragment(buyFragmentFactory.buyFragment(token))
    }

    override fun navigateToNewBuyScreen(token: Token, fiatToken: String, fiatAmount: String?) {
        replaceFragment(NewBuyFragment.create(token, fiatToken, fiatAmount))
    }

    override fun navigateToKycStatus(status: StrigaKycStatusBanner) {
        if (status == StrigaKycStatusBanner.VERIFICATION_DONE) {
            StrigaUserIbanDetailsFragment.create()
        } else {
            strigaKycFragmentFactory.kycFragment()
        }.also(::replaceFragment)
    }

    override fun navigateToStrigaClaimOtp(usdAmount: String, challengeId: StrigaWithdrawalChallengeId) {
        val fragment = strigaKycFragmentFactory.claimOtpFragment(
            titleAmount = usdAmount,
            challengeId = challengeId
        )
        replaceFragmentForResult(fragment, StrigaOnRampSmsInputFragment.REQUEST_KEY, onResult = { _, _ ->
            Timber.d("Striga claim OTP: success")
            // todo: show success claim bottomsheet
        })
    }

    override fun showKycPendingDialog() {
        StrigaKycPendingBottomSheet.show(parentFragmentManager)
    }

    override fun showTopupWalletDialog() {
        TopUpWalletBottomSheet.show(parentFragmentManager)
    }

    override fun showSendNoTokens(fallbackToken: Token) {
        replaceFragment(SendUnavailableFragment.create(fallbackToken))
    }

    override fun showTokenClaim(token: Token.Eth) {
        replaceFragment(ClaimFragment.create(ethereumToken = token))
    }

    override fun showProgressDialog(bundleId: String, progressDetails: NewShowProgress) {
        listener?.showTransactionProgress(bundleId, progressDetails)
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
            viewBalance.root.isVisible = !isEmpty

            val updatedAdapter = if (isEmpty) emptyAdapter else contentAdapter
            if (homeRecyclerView.adapter != updatedAdapter) {
                homeRecyclerView.adapter = updatedAdapter
            }
            homeRecyclerView.setBackgroundColor(getColor(if (isEmpty) R.color.bg_smoke else R.color.bg_snow))
        }
    }

    override fun navigateToProfile() {
        replaceFragment(SettingsFragment.create())
    }

    override fun navigateToReserveUsername() {
        replaceFragment(ReserveUsernameFragment.create(from = ReserveUsernameOpenedFrom.SETTINGS))
    }

    override fun onBannerClicked(bannerTitleId: Int) {
        presenter.onBannerClicked(bannerTitleId)
    }

    override fun onStrigaClaimTokenClicked(item: HomeElementItem.StrigaClaim) {
        presenter.onStrigaClaimTokenClicked(item)
    }

    override fun showStrigaClaimProgress(isClaimInProgress: Boolean, tokenMint: Base58String) {
        contentAdapter.updateItem<HomeElementItem.StrigaClaim>(
            itemFilter = { item ->
                item is HomeElementItem.StrigaClaim && item.tokenMintAddress == tokenMint
            },
            transform = {
                it.copy(isClaimInProcess = isClaimInProgress)
            }
        )
    }

    override fun onToggleClicked() {
        homeAnalytics.logHiddenTokensClicked()
        presenter.toggleTokenVisibilityState()
    }

    override fun onTokenClicked(token: Token.Active) {
        homeAnalytics.logMainScreenTokenDetailsOpen(tokenTier = token.tokenSymbol)
        replaceFragment(TokenHistoryFragment.create(token))
    }

    override fun onPopularTokenClicked(token: Token) {
        presenter.onBuyTokenClicked(token)
    }

    override fun onHideClicked(token: Token.Active) {
        homeAnalytics.logHiddenTokensClicked()
        presenter.toggleTokenVisibility(token)
    }

    override fun onClaimTokenClicked(canBeClaimed: Boolean, token: Token.Eth) {
        presenter.onClaimClicked(canBeClaimed, token)
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
