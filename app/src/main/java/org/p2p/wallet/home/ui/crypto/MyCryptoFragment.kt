package org.p2p.wallet.home.ui.crypto

import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.SimpleItemAnimator
import android.content.Context
import android.os.Bundle
import android.view.View
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import timber.log.Timber
import org.p2p.core.glide.GlideManager
import org.p2p.core.token.Token
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.recycler.decoration.GroupedRoundingDecoration
import org.p2p.uikit.utils.recycler.decoration.topOffsetDifferentClassDecoration
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bindOrGone
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.ui.ClaimFragment
import org.p2p.wallet.bridge.send.SendFragmentFactory
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.databinding.FragmentMyCryptoBinding
import org.p2p.wallet.debug.settings.DebugSettingsFragment
import org.p2p.wallet.history.ui.token.TokenHistoryFragment
import org.p2p.wallet.home.onofframp.OnOffRampNavigator
import org.p2p.wallet.home.ui.crypto.bottomsheet.TokenVisibilityChangeBottomSheet
import org.p2p.wallet.home.ui.main.delegates.bridgeclaim.EthClaimTokenCellModel
import org.p2p.wallet.home.ui.main.delegates.bridgeclaim.ethClaimTokenDelegate
import org.p2p.wallet.home.ui.main.delegates.hidebutton.tokenButtonDelegate
import org.p2p.wallet.home.ui.main.delegates.token.TokenCellModel
import org.p2p.wallet.home.ui.main.delegates.token.tokenDelegate
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.jupiter.ui.main.JupiterSwapFragment
import org.p2p.wallet.receive.ReceiveFragmentFactory
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.send.ui.SearchOpenedFromScreen
import org.p2p.wallet.send.ui.search.NewSearchFragment
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.progresshandler.ClaimProgressHandler
import org.p2p.wallet.utils.HomeScreenLayoutManager
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.getParcelableCompat
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

private const val KEY_REQUEST = "KEY_REQUEST"
private const val KEY_RESULT_TOKEN = "KEY_RESULT_TOKEN"

class MyCryptoFragment :
    BaseMvpFragment<MyCryptoContract.View, MyCryptoContract.Presenter>(R.layout.fragment_my_crypto),
    MyCryptoContract.View {

    companion object {
        fun create(): MyCryptoFragment = MyCryptoFragment()
    }

    override val presenter: MyCryptoContract.Presenter by inject()

    private val binding: FragmentMyCryptoBinding by viewBinding()

    private val receiveFragmentFactory: ReceiveFragmentFactory by inject()
    private val glideManager: GlideManager by inject()
    private val onOffRampNavigator: OnOffRampNavigator by inject()
    private val sendFragmentFactory: SendFragmentFactory by inject()

    private var listener: RootListener? = null

    private val cellAdapter = CommonAnyCellAdapter(
        tokenDelegate(glideManager) { binding, item ->
            with(binding.contentView) {
                setOnClickListener { presenter.onTokenClicked(item.payload) }
                setOnLongClickListener {
                    showTokenVisibilityStateChangeDialog(item)
                    true
                }
            }
        },
        tokenButtonDelegate() { binding, _ ->
            binding.root.setOnClickListener { onVisibilityToggleClicked() }
        },
        ethClaimTokenDelegate(glideManager) { binding, item ->
            with(binding) {
                contentView.setOnClickListener { onClaimTokenClicked(item.isClaimEnabled, item.payload) }
                buttonClaim.setOnClickListener { onClaimTokenClicked(item.isClaimEnabled, item.payload) }
            }
        },
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? RootListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupView()
        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST,
            viewLifecycleOwner,
            ::onFragmentResult
        )
    }

    override fun showActionButtons(buttons: List<ActionButton>) {
        binding.viewActionButtons.showActionButtons(buttons)
    }

    override fun showTokenHistory(token: Token.Active) {
        replaceFragment(TokenHistoryFragment.create(token))
    }

    private fun showTokenVisibilityStateChangeDialog(item: TokenCellModel) {
        if (item.payload.canTokenBeHidden) {
            TokenVisibilityChangeBottomSheet.show(
                fm = childFragmentManager,
                token = item.payload,
                isTokenHidden = item.isDefinitelyHidden,
                requestKey = KEY_REQUEST,
                resultKey = KEY_RESULT_TOKEN
            )
        }
    }

    private fun FragmentMyCryptoBinding.setupView() {
        recyclerViewCrypto.apply {
            layoutManager = HomeScreenLayoutManager(requireContext())
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            attachAdapter(cellAdapter)
            addItemDecoration(GroupedRoundingDecoration(TokenCellModel::class, 16f.toPx()))
            addItemDecoration(topOffsetDifferentClassDecoration())
            addItemDecoration(GroupedRoundingDecoration(EthClaimTokenCellModel::class, 16f.toPx()))
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

    private fun onActionButtonClicked(clickedButton: ActionButton) {
        when (clickedButton) {
            ActionButton.BUY_BUTTON -> {
                presenter.onBuyClicked()
            }
            ActionButton.RECEIVE_BUTTON -> {
                presenter.onReceiveClicked()
            }
            ActionButton.SEND_BUTTON -> {
                presenter.onSendClicked()
            }
            ActionButton.SWAP_BUTTON -> {
                presenter.onSwapClicked()
            }
            else -> Timber.d("Unsupported Action! $clickedButton")
        }
    }

    override fun showAddMoneyDialog() {
        onOffRampNavigator.navigateToAddMoney(this)
    }

    override fun navigateToSend() {
        val target = NewSearchFragment.create(SearchOpenedFromScreen.MAIN)
        replaceFragment(target)
    }

    override fun showBalance(cellModel: TextViewCellModel?) {
        binding.viewBalance.textViewAmount.bindOrGone(cellModel)
    }

    override fun showUserAddress(ellipsizedAddress: String) {
        with(binding.layoutToolbar) {
            textViewAddress.text = "\uD83D\uDD17 $ellipsizedAddress"
            textViewAddress.setOnClickListener {
                presenter.onAddressClicked()
            }
        }
    }

    override fun showAddressCopied(addressOrUsername: String, @StringRes stringResId: Int) {
        requireContext().copyToClipBoard(addressOrUsername)
        showUiKitSnackBar(
            message = getString(stringResId),
            actionButtonResId = R.string.common_ok,
            actionBlock = Snackbar::dismiss
        )
    }

    override fun showRefreshing(isRefreshing: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = isRefreshing
    }

    override fun showItems(items: List<AnyCellItem>) {
        cellAdapter.setItems(items) {
            binding.recyclerViewCrypto.invalidateItemDecorations()
        }
    }

    override fun showEmptyState(isEmpty: Boolean) {
        with(binding) {
            textViewEmpty.isVisible = isEmpty
            recyclerViewCrypto.isVisible = !isEmpty
        }
        setAppBarScrollingState(!isEmpty)
    }

    private fun setAppBarScrollingState(isScrollingEnabled: Boolean) {
        with(binding) {
            collapsingToolbar.updateLayoutParams<AppBarLayout.LayoutParams> {
                scrollFlags = if (isScrollingEnabled) {
                    SCROLL_FLAG_SCROLL
                } else {
                    SCROLL_FLAG_NO_SCROLL
                }
            }
            if (!isScrollingEnabled) {
                appBarLayout.setExpanded(true, false)
            }
        }
    }

    override fun navigateToReceive() {
        replaceFragment(receiveFragmentFactory.receiveFragment())
    }

    override fun navigateToSwap() {
        replaceFragment(JupiterSwapFragment.create(source = SwapOpenedFrom.MAIN_SCREEN))
    }

    private fun onVisibilityToggleClicked() {
        presenter.toggleTokenVisibilityState()
    }

    private fun onHideClicked(token: Token.Active) {
        presenter.toggleTokenVisibility(token)
    }

    private fun onClaimTokenClicked(canBeClaimed: Boolean, token: Token.Eth) {
        presenter.onClaimClicked(canBeClaimed, token)
    }

    override fun navigateToTokenClaim(token: Token.Eth) {
        replaceFragment(ClaimFragment.create(ethereumToken = token))
    }

    override fun showProgressDialog(bundleId: String, progressDetails: NewShowProgress) {
        listener?.showTransactionProgress(bundleId, progressDetails, ClaimProgressHandler.QUALIFIER)
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        if (result.containsKey(KEY_RESULT_TOKEN)) {
            result.getParcelableCompat<Token.Active>(KEY_RESULT_TOKEN)?.let {
                onHideClicked(it)
            }
        }
    }
}
