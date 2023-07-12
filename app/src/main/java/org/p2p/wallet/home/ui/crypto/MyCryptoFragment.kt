package org.p2p.wallet.home.ui.crypto

import androidx.core.view.isVisible
import android.content.Context
import android.os.Bundle
import android.view.View
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
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.ui.ClaimFragment
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.databinding.FragmentMyCryptoBinding
import org.p2p.wallet.history.ui.token.TokenHistoryFragment
import org.p2p.wallet.home.analytics.HomeAnalytics
import org.p2p.wallet.home.ui.main.delegates.bridgeclaim.EthClaimTokenCellModel
import org.p2p.wallet.home.ui.main.delegates.bridgeclaim.ethClaimTokenDelegate
import org.p2p.wallet.home.ui.main.delegates.hidebutton.tokenButtonDelegate
import org.p2p.wallet.home.ui.main.delegates.token.TokenCellModel
import org.p2p.wallet.home.ui.main.delegates.token.tokenDelegate
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.jupiter.ui.main.JupiterSwapFragment
import org.p2p.wallet.receive.ReceiveFragmentFactory
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.utils.HomeScreenLayoutManager
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

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
    private val homeAnalytics: HomeAnalytics by inject()

    private var listener: RootListener? = null

    private val cellAdapter = CommonAnyCellAdapter(
        tokenDelegate(glideManager) { binding, item ->
            with(binding) {
                imageViewHideToken.setOnClickListener {
                    onHideClicked(item.payload)
                    binding.root.close(animation = true)
                }
                contentView.setOnClickListener { onTokenClicked(item.payload) }
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
        }
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? RootListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupView()
    }

    override fun showActionButtons(buttons: List<ActionButton>) {
        binding.viewActionButtons.showActionButtons(buttons)
    }

    private fun FragmentMyCryptoBinding.setupView() {
        recyclerViewCrypto.apply {
            layoutManager = HomeScreenLayoutManager(requireContext())
            attachAdapter(cellAdapter)
            addItemDecoration(GroupedRoundingDecoration(TokenCellModel::class, 16f.toPx()))
            addItemDecoration(topOffsetDifferentClassDecoration())
            addItemDecoration(GroupedRoundingDecoration(EthClaimTokenCellModel::class, 16f.toPx()))
        }
        swipeRefreshLayout.setOnRefreshListener(presenter::refreshTokens)
        viewActionButtons.onButtonClicked = ::onActionButtonClicked
    }

    private fun onActionButtonClicked(clickedButton: ActionButton) {
        when (clickedButton) {
            ActionButton.RECEIVE_BUTTON -> {
                presenter.onReceiveClicked()
            }
            ActionButton.SWAP_BUTTON -> {
                presenter.onSwapClicked()
            }
            else -> Timber.d("Unsupported Action! $clickedButton")
        }
    }

    override fun showBalance(cellModel: TextViewCellModel?) {
        binding.viewBalance.textViewAmount.bindOrGone(cellModel)
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
    }

    override fun navigateToReceive() {
        replaceFragment(receiveFragmentFactory.receiveFragment())
    }

    override fun navigateToSwap() {
        replaceFragment(JupiterSwapFragment.create(source = SwapOpenedFrom.MAIN_SCREEN))
    }

    private fun onVisibilityToggleClicked() {
        homeAnalytics.logHiddenTokensClicked()
        presenter.toggleTokenVisibilityState()
    }

    private fun onTokenClicked(token: Token.Active) {
        homeAnalytics.logMainScreenTokenDetailsOpen(tokenTier = token.tokenSymbol)
        replaceFragment(TokenHistoryFragment.create(token))
    }

    private fun onHideClicked(token: Token.Active) {
        homeAnalytics.logHiddenTokensClicked()
        presenter.toggleTokenVisibility(token)
    }

    private fun onClaimTokenClicked(canBeClaimed: Boolean, token: Token.Eth) {
        presenter.onClaimClicked(canBeClaimed, token)
    }

    override fun navigateToTokenClaim(token: Token.Eth) {
        replaceFragment(ClaimFragment.create(ethereumToken = token))
    }

    override fun showProgressDialog(bundleId: String, progressDetails: NewShowProgress) {
        listener?.showTransactionProgress(bundleId, progressDetails)
    }
}
