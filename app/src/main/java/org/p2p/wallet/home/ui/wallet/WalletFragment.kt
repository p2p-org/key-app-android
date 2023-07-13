package org.p2p.wallet.home.ui.wallet

import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import timber.log.Timber
import org.p2p.core.crypto.Base58String
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.recycler.decoration.GroupedRoundingDecoration
import org.p2p.uikit.utils.recycler.decoration.topOffsetDifferentClassDecoration
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bindOrGone
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameFragment
import org.p2p.wallet.auth.ui.reserveusername.ReserveUsernameOpenedFrom
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.databinding.FragmentWalletBinding
import org.p2p.wallet.databinding.LayoutHomeToolbarBinding
import org.p2p.wallet.debug.settings.DebugSettingsFragment
import org.p2p.wallet.home.ui.main.delegates.banner.homeScreenBannerDelegate
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.StrigaOnRampCellModel
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.strigaOnRampTokenDelegate
import org.p2p.wallet.kyc.StrigaFragmentFactory
import org.p2p.wallet.kyc.model.StrigaBanner
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.newsend.ui.SearchOpenedFromScreen
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.striga.iban.StrigaUserIbanDetailsFragment
import org.p2p.wallet.striga.sms.onramp.StrigaOtpConfirmFragment
import org.p2p.wallet.striga.ui.TopUpWalletBottomSheet
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.utils.HomeScreenLayoutManager
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.replaceFragmentForResult
import org.p2p.wallet.utils.toPx
import org.p2p.wallet.utils.viewbinding.viewBinding

class WalletFragment :
    BaseMvpFragment<WalletContract.View, WalletContract.Presenter>(R.layout.fragment_wallet),
    WalletContract.View {

    companion object {
        fun create(): WalletFragment = WalletFragment()
    }

    override val presenter: WalletContract.Presenter by inject()

    private val binding: FragmentWalletBinding by viewBinding()

    private val receiveAnalytics: ReceiveAnalytics by inject()
    private val glideManager: GlideManager by inject()
    private val strigaFragmentFactory: StrigaFragmentFactory by inject()

    private val cellAdapter = CommonAnyCellAdapter(
        strigaOnRampTokenDelegate(
            glideManager = glideManager,
            onBindListener = { binding, item ->
                binding.buttonClaim.setOnClickListener {
                    presenter.onStrigaOnRampClicked(item)
                }
            }
        ),
        homeScreenBannerDelegate<StrigaBanner> { binding, item ->
            with(binding) {
                buttonAction.setOnClickListener { presenter.onStrigaBannerClicked(item) }
                root.setOnClickListener { presenter.onStrigaBannerClicked(item) }
            }
        }
    )

    override fun showKycPendingDialog() {
        strigaFragmentFactory.showPendingBottomSheet(parentFragmentManager)
    }

    override fun showStrigaBannerProgress(isLoading: Boolean) {
        cellAdapter.updateItem<StrigaBanner>(
            predicate = { it is StrigaBanner },
            transform = { it.copy(isLoading = isLoading) }
        )
    }

    override fun setCellItems(items: List<AnyCellItem>) {
        Timber.d("Set cell items: ${items.map { it::class.simpleName }}")
        cellAdapter.setItems(items) {
            binding.recyclerViewHome.invalidateItemDecorations()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupView()
    }

    override fun showAddressCopied(addressOrUsername: String) {
        requireContext().copyToClipBoard(addressOrUsername)
        showUiKitSnackBar(
            message = getString(R.string.home_address_snackbar_text),
            actionButtonResId = R.string.common_ok,
            actionBlock = Snackbar::dismiss
        )
    }

    override fun showActionButtons(buttons: List<ActionButton>) {
        binding.viewActionButtons.showActionButtons(buttons)
    }

    private fun FragmentWalletBinding.setupView() {
        layoutToolbar.setupToolbar()

        recyclerViewHome.apply {
            layoutManager = HomeScreenLayoutManager(requireContext())
            attachAdapter(cellAdapter)
            addItemDecoration(topOffsetDifferentClassDecoration())
            addItemDecoration(GroupedRoundingDecoration(StrigaOnRampCellModel::class, 16f.toPx()))
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
        // todo: remove unused buttons
        when (clickedButton) {
            ActionButton.BUY_BUTTON -> {
                presenter.onTopupClicked()
            }
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
            else -> Unit
        }
    }

    override fun showUserAddress(ellipsizedAddress: String) {
        binding.layoutToolbar.textViewAddress.text = ellipsizedAddress
    }

    override fun showBalance(cellModel: TextViewCellModel?) {
        binding.viewBalance.textViewAmount.bindOrGone(cellModel)
    }

    override fun showRefreshing(isRefreshing: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = isRefreshing
    }

    override fun showTopupWalletDialog() {
        TopUpWalletBottomSheet.show(parentFragmentManager)
    }

    override fun showStrigaOnRampProgress(isLoading: Boolean, tokenMint: Base58String) {
        binding.recyclerViewHome.post {
            cellAdapter.updateItem<StrigaOnRampCellModel>(
                predicate = { item ->
                    item is StrigaOnRampCellModel && item.tokenMintAddress == tokenMint
                },
                transform = { it.copy(isLoading = isLoading) }
            )
        }
    }

    override fun navigateToProfile() {
        replaceFragment(SettingsFragment.create())
    }

    override fun navigateToReserveUsername() {
        replaceFragment(ReserveUsernameFragment.create(from = ReserveUsernameOpenedFrom.SETTINGS))
    }

    override fun navigateToStrigaOnRampOtp(usdAmount: String, challengeId: StrigaWithdrawalChallengeId) {
        val fragment = strigaFragmentFactory.onRampConfirmOtpFragment(
            titleAmount = usdAmount,
            challengeId = challengeId
        )
        replaceFragmentForResult(fragment, StrigaOtpConfirmFragment.REQUEST_KEY, onResult = { _, _ ->
            Timber.d("Striga claim OTP: success")
            // todo: show success claim bottomsheet
        })
    }

    override fun navigateToStrigaByBanner(status: StrigaKycStatusBanner) {
        if (status == StrigaKycStatusBanner.VERIFICATION_DONE) {
            StrigaUserIbanDetailsFragment.create()
        } else {
            strigaFragmentFactory.kycFragment()
        }.also(::replaceFragment)
    }
}
