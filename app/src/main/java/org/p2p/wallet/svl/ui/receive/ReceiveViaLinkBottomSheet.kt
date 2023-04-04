package org.p2p.wallet.svl.ui.receive

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.drawable.UiKitDrawableCellModels
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.image.bind
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bind
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogSendViaLinkReceiveFundsBinding
import org.p2p.wallet.svl.interactor.SendViaLinkWrapper
import org.p2p.wallet.svl.model.SendViaLinkClaimingState
import org.p2p.wallet.svl.ui.error.SendViaLinkError
import org.p2p.wallet.svl.ui.error.SendViaLinkErrorFragment
import org.p2p.wallet.svl.ui.send.SvlReceiveFundsAnalytics
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.doOnAnimationEnd
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone

private const val ARG_LINK = "ARG_LINK"

class ReceiveViaLinkBottomSheet :
    BaseMvpBottomSheet<ReceiveViaLinkContract.View, ReceiveViaLinkContract.Presenter>(
        layoutRes = R.layout.dialog_send_via_link_receive_funds
    ),
    ReceiveViaLinkContract.View {
    companion object {
        fun show(fm: FragmentManager, link: SendViaLinkWrapper) {
            ReceiveViaLinkBottomSheet()
                .withArgs(ARG_LINK to link)
                .show(fm, ReceiveViaLinkBottomSheet::javaClass.name)
        }
    }

    private val link: SendViaLinkWrapper by args(ARG_LINK)

    private val binding: DialogSendViaLinkReceiveFundsBinding by viewBinding()

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override val presenter: ReceiveViaLinkContract.Presenter by inject()
    private val analytics: SvlReceiveFundsAnalytics by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analytics.logClaimStartedOpened()

        UiKitDrawableCellModels.shapeCircleWithTint(R.color.bg_rain)
            .applyBackground(binding.layoutClaimSuccess.imageViewIcon)

        presenter.parseAccountFromLink(link)
    }

    override fun renderClaimTokenDetails(
        tokenAmount: TextViewCellModel,
        sentFromAddress: TextViewCellModel,
        tokenIcon: ImageViewCellModel
    ) = with(binding) {
        imageViewTokenIcon.bind(tokenIcon)
        textViewTokenAmount.bind(tokenAmount)
    }

    override fun renderState(state: SendViaLinkClaimingState) = with(binding) {
        when (state) {
            is SendViaLinkClaimingState.InitialLoading -> {
                layoutTransactionDetails.isVisible = false
                progressStateTransaction.isVisible = false
                imageViewBanner.isVisible = false
                progressBar.isVisible = true
                buttonDone.isVisible = false
            }
            is SendViaLinkClaimingState.ReadyToClaim -> {
                layoutTransactionDetails.isVisible = true
                progressBar.isVisible = false
                progressStateTransaction.isVisible = false
                imageViewBanner.isVisible = false
                buttonDone.isVisible = true
                buttonDone.setText(R.string.common_confirm)
                buttonDone.setOnClickListener {
                    analytics.logClaimConfirmStarted(
                        temporaryAccount = state.temporaryAccount,
                        tokenSymbol = state.token.tokenSymbol,
                        tokenAmount = state.token.total.toPlainString(),
                        // TODO https://p2pvalidator.atlassian.net/browse/PWN-8003 pass when we will be able to parse link author
                        linkAuthor = emptyString()
                    )

                    presenter.claimToken(state.temporaryAccount, state.token)
                }
            }
            is SendViaLinkClaimingState.ClaimingInProcess -> {
                layoutClaimSuccess.root.isVisible = false
                layoutTransactionDetails.isVisible = true
                imageViewBanner.isVisible = false
                progressStateTransaction.isVisible = true
                progressStateTransaction.setDescriptionText(R.string.transaction_description_progress)
                buttonDone.setText(R.string.common_close)
                buttonDone.setOnClickListener {
                    analytics.logCloseClicked()
                    dismissAllowingStateLoss()
                }
            }
            is SendViaLinkClaimingState.ClaimSuccess -> {
                analytics.logClaimSuccess()

                layoutClaimSuccess.root.isVisible = true
                textViewTitle.isVisible = false
                layoutTransactionDetails.isVisible = false
                progressStateTransaction.isVisible = false
                imageViewBanner.isVisible = false
                layoutClaimSuccess.textViewTitle.bind(state.successMessage)
                playApplauseAnimation()
                buttonDone.setText(R.string.send_via_link_receive_funds_success_button)
                buttonDone.setOnClickListener {
                    analytics.logClaimGotItClicked()
                    dismissAllowingStateLoss()
                }
            }
            is SendViaLinkClaimingState.ClaimFailed -> {
                analytics.logClaimFailed()

                layoutTransactionDetails.isVisible = true
                progressStateTransaction.isVisible = true
                progressBar.isVisible = false
                imageViewBanner.isVisible = false
                progressStateTransaction.setDescriptionText(R.string.transaction_description_failed)
                progressStateTransaction.setFailedState()
                buttonDone.isVisible = true
                buttonDone.setText(R.string.common_close)
                buttonDone.setOnClickListener {
                    analytics.logCloseClicked()
                    dismissAllowingStateLoss()
                }
            }
            is SendViaLinkClaimingState.ParsingFailed -> {
                textViewTitle.setText(state.titleRes)
                textViewSubtitle withTextOrGone state.subTitleRes?.let { getString(it) }
                imageViewBanner.setImageResource(state.iconRes)
                progressBar.isVisible = false
                imageViewBanner.isVisible = true
                progressStateTransaction.isVisible = false
                layoutClaimSuccess.root.isVisible = false

                buttonDone.isVisible = true
                buttonDone.setText(R.string.common_reload)
                buttonDone.setOnClickListener { presenter.parseAccountFromLink(link, isRetry = true) }
            }
        }
    }

    override fun showButtonLoading(isLoading: Boolean) {
        binding.buttonDone.isLoadingState = isLoading
    }

    override fun showLinkError(error: SendViaLinkError) {
        dismissAllowingStateLoss()
        replaceFragment(SendViaLinkErrorFragment.create(error))
    }

    private fun playApplauseAnimation() {
        with(binding.animationView) {
            setAnimation(R.raw.raw_animation_applause)
            isVisible = true
            doOnAnimationEnd { isVisible = false }
            playAnimation()
        }
    }
}
