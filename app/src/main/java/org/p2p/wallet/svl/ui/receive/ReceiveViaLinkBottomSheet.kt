package org.p2p.wallet.svl.ui.receive

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.loadUrl
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogSendViaLinkReceiveFundsBinding
import org.p2p.wallet.svl.interactor.SendViaLinkWrapper
import org.p2p.wallet.svl.model.SendViaLinkClaimingState
import org.p2p.wallet.svl.ui.error.SendViaLinkError
import org.p2p.wallet.svl.ui.error.SendViaLinkErrorFragment
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.doOnAnimationEnd
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_LINK = "ARG_LINK"

class SendViaLinkReceiveFundsBottomSheet :
    BaseMvpBottomSheet<ReceiveViaLinkContract.View, ReceiveViaLinkContract.Presenter>(
        layoutRes = R.layout.dialog_send_via_link_receive_funds
    ),
    ReceiveViaLinkContract.View {
    companion object {
        fun show(fm: FragmentManager, link: SendViaLinkWrapper) {
            SendViaLinkReceiveFundsBottomSheet()
                .withArgs(ARG_LINK to link)
                .show(fm, SendViaLinkReceiveFundsBottomSheet::javaClass.name)
        }
    }

    private val link: SendViaLinkWrapper by args(ARG_LINK)
    private val binding: DialogSendViaLinkReceiveFundsBinding by viewBinding()
    private val glideManager: GlideManager by inject()

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override val presenter: ReceiveViaLinkContract.Presenter by inject { parametersOf(link) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DrawableCellModel(
            drawable = shapeDrawable(shapeCircle()),
            tint = R.color.bg_rain
        ).applyBackground(binding.layoutClaimSuccess.imageViewIcon)
    }

    override fun renderClaimTokenDetails(
        amountInTokens: String,
        tokenSymbol: String,
        sentFromAddress: Base58String,
        tokenIconUrl: String,
        linkCreationDate: String
    ) = with(binding) {
        textViewSubtitle.text = linkCreationDate
        imageViewTokenIcon.loadUrl(glideManager, tokenIconUrl, circleCrop = true)
        textViewTokenAmount.text = "$amountInTokens $tokenSymbol"
    }

    override fun renderState(state: SendViaLinkClaimingState) = with(binding) {
        when (state) {
            is SendViaLinkClaimingState.ReadyToClaim -> {
                groupTitle.isVisible = true
                layoutTransactionDetails.isVisible = true
                progressStateTransaction.isVisible = false
                imageViewBanner.isVisible = false
                buttonDone.setText(R.string.common_confirm)
                buttonDone.setOnClickListener {
                    presenter.claimToken(state.temporaryAccount, state.amountInTokens, state.tokenSymbol)
                }
            }
            is SendViaLinkClaimingState.ClaimingInProcess -> {
                layoutClaimSuccess.root.isVisible = false
                progressStateTransaction.isVisible = true
                groupTitle.isVisible = true
                layoutTransactionDetails.isVisible = true
                imageViewBanner.isVisible = false
                progressStateTransaction.setDescriptionText(R.string.transaction_description_progress)
                buttonDone.setText(R.string.common_close)
                buttonDone.setOnClickListener { dismissAllowingStateLoss() }
            }
            is SendViaLinkClaimingState.ClaimSuccess -> {
                layoutClaimSuccess.root.isVisible = true
                groupTitle.isVisible = false
                layoutTransactionDetails.isVisible = false
                progressStateTransaction.isVisible = false
                imageViewBanner.isVisible = false
                layoutClaimSuccess.textViewTitle.text = getString(
                    R.string.send_via_link_receive_funds_success_title,
                    state.tokenAmount,
                    state.tokenSymbol
                )
                playApplauseAnimation()
                buttonDone.setText(R.string.send_via_link_receive_funds_success_button)
                buttonDone.setOnClickListener { dismissAllowingStateLoss() }
            }
            is SendViaLinkClaimingState.ClaimFailed -> {
                groupTitle.isVisible = true
                layoutTransactionDetails.isVisible = true
                progressStateTransaction.isVisible = true
                imageViewBanner.isVisible = false
                progressStateTransaction.setDescriptionText(R.string.transaction_description_failed)
                progressStateTransaction.setFailedState()
                buttonDone.setText(R.string.common_close)
                buttonDone.setOnClickListener { dismissAllowingStateLoss() }
            }
            is SendViaLinkClaimingState.ParsingFailed -> {
                groupTitle.isVisible = true
                imageViewBanner.isVisible = true
                progressStateTransaction.isVisible = false
                layoutClaimSuccess.root.isVisible = false

                buttonDone.setText(R.string.common_reload)
                buttonDone.setOnClickListener { presenter.parseLink(link) }
            }
        }
    }

    override fun navigateToErrorScreen(error: SendViaLinkError) {
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
