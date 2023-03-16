package org.p2p.wallet.home.ui.vialink

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.loadUrl
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogSendViaLinkRecieveFundsBinding
import org.p2p.wallet.home.ui.vialink.interactor.SendViaLinkWrapper
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.doOnAnimationEnd
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_LINK = "ARG_LINK"

class SendViaLinkReceiveFundsBottomSheet :
    BaseMvpBottomSheet<SendViaLinkReceiveFundsContract.View, SendViaLinkReceiveFundsContract.Presenter>(
        layoutRes = R.layout.dialog_send_via_link_recieve_funds
    ),
    SendViaLinkReceiveFundsContract.View {
    companion object {
        fun show(fm: FragmentManager, link: SendViaLinkWrapper) {
            SendViaLinkReceiveFundsBottomSheet()
                .withArgs(ARG_LINK to link)
                .show(fm, SendViaLinkReceiveFundsBottomSheet::javaClass.name)
        }
    }

    private val link: SendViaLinkWrapper by args(ARG_LINK)
    private val binding: DialogSendViaLinkRecieveFundsBinding by viewBinding()
    private val glideManager: GlideManager by inject()

    override val presenter: SendViaLinkReceiveFundsContract.Presenter by inject { parametersOf(link) }

    override fun renderClaimTokenDetails(
        amountInTokens: String,
        tokenSymbol: String,
        sentFromAddress: Base58String,
        addressAsUsername: String?,
        tokenIconUrl: String,
        linkCreationDate: String
    ) = with(binding) {
        textViewSubtitle.text = linkCreationDate
        imageViewTokenIcon.loadUrl(glideManager, tokenIconUrl)
        textViewTokenAmount.text = "$amountInTokens $tokenSymbol"
        textViewSentFromValue.text = addressAsUsername ?: sentFromAddress.base58Value.cutMiddle()
    }

    override fun renderState(state: SendViaLinkReceiveFundsState) = with(binding) {
        when (state) {
            is SendViaLinkReceiveFundsState.ReadyToClaim -> {
                groupContent.isVisible = true
                progressStateTransaction.isVisible = false
                buttonDone.setText(R.string.common_confirm)
                buttonDone.setOnClickListener { presenter.claimToken() }
            }
            is SendViaLinkReceiveFundsState.ClaimingInProcess -> {
                groupContent.isVisible = true
                progressStateTransaction.isVisible = true
                progressStateTransaction.setDescriptionText(R.string.transaction_description_progress)
                buttonDone.setText(R.string.common_close)
                buttonDone.setOnClickListener { dismiss() }
            }
            is SendViaLinkReceiveFundsState.ClaimSuccess -> {
                groupContent.isVisible = false
                layoutClaimSuccess.root.isVisible = true
                playApplauseAnimation()
                buttonDone.setText(R.string.send_via_link_receive_funds_success_button)
                buttonDone.setOnClickListener { dismiss() }
            }
            is SendViaLinkReceiveFundsState.ClaimFailed -> {
                groupContent.isVisible = true
                progressStateTransaction.isVisible = true
                progressStateTransaction.setDescriptionText(R.string.transaction_description_failed)
                buttonDone.setText(R.string.common_close)
                buttonDone.setOnClickListener { dismiss() }
            }
        }
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
