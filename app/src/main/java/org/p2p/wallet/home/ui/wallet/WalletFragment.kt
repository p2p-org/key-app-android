package org.p2p.wallet.home.ui.wallet

import androidx.core.view.isVisible
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import timber.log.Timber
import org.p2p.core.crypto.Base58String
import org.p2p.core.utils.asUsd
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
import org.p2p.wallet.databinding.FragmentWalletBinding
import org.p2p.wallet.databinding.LayoutHomeToolbarBinding
import org.p2p.wallet.debug.settings.DebugSettingsFragment
import org.p2p.wallet.home.ui.main.delegates.banner.homeScreenBannerDelegate
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.StrigaOnRampCellModel
import org.p2p.wallet.home.ui.main.delegates.striga.onramp.strigaOnRampTokenDelegate
import org.p2p.wallet.home.ui.topup.TopUpWalletBottomSheet
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaBanner
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaKycStatusBanner
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.striga.StrigaFragmentFactory
import org.p2p.wallet.striga.offramp.ui.StrigaOffRampFragment
import org.p2p.wallet.striga.onramp.iban.StrigaUserIbanDetailsFragment
import org.p2p.wallet.striga.sms.onramp.StrigaOtpConfirmFragment
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
    private val strigaFragmentFactory: StrigaFragmentFactory by inject()

    private val cellAdapter = CommonAnyCellAdapter(
        strigaOnRampTokenDelegate(
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

    private fun FragmentWalletBinding.setupView() {
        layoutToolbar.setupToolbar()

        recyclerViewHome.apply {
            layoutManager = HomeScreenLayoutManager(requireContext())
            attachAdapter(cellAdapter)
            addItemDecoration(topOffsetDifferentClassDecoration())
            addItemDecoration(GroupedRoundingDecoration(StrigaOnRampCellModel::class, 16f.toPx()))
        }

        swipeRefreshLayout.setOnRefreshListener(presenter::refreshTokens)
        buttonAddMoney.setOnClickListener {
            presenter.onAddMoneyClicked()
        }
        buttonWithdraw.setOnClickListener {
            presenter.onWithdrawClicked()
        }

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

    override fun navigateToOffRamp() {
        replaceFragment(StrigaOffRampFragment())
    }

    private fun LayoutHomeToolbarBinding.setupToolbar() {
        textViewAddress.setOnClickListener {
            receiveAnalytics.logAddressOnMainClicked()
            presenter.onAddressClicked()
        }
        imageViewProfile.setOnClickListener { presenter.onProfileClick() }
        imageViewQr.setOnClickListener { replaceFragment(ReceiveSolanaFragment.create(token = null)) }
    }

    override fun showUserAddress(ellipsizedAddress: String) {
        binding.layoutToolbar.textViewAddress.text = ellipsizedAddress
    }

    override fun showBalance(fiatBalanceCellModel: TextViewCellModel?, tokenBalanceCellModel: TextViewCellModel?) {
        binding.viewBalance.textViewAmount.bindOrGone(fiatBalanceCellModel)
        binding.textViewTokenAmount.bindOrGone(fiatBalanceCellModel)
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

    override fun navigateToStrigaOnRampConfirmOtp(
        challengeId: StrigaWithdrawalChallengeId,
        token: StrigaOnRampCellModel
    ) {
        val fragment = strigaFragmentFactory.onRampConfirmOtpFragment(
            titleAmount = token.amountAvailable.asUsd(),
            challengeId = challengeId
        )
        replaceFragmentForResult(fragment, StrigaOtpConfirmFragment.REQUEST_KEY, onResult = { _, bundle ->
            if (bundle.getBoolean(StrigaOtpConfirmFragment.RESULT_KEY_CONFIRMED, false)) {
                presenter.onOnRampConfirmed(challengeId, token)
            }
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
