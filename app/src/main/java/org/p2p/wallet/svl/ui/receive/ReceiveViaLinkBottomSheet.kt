package org.p2p.wallet.svl.ui.receive

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import android.content.Context
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
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.svl.interactor.SendViaLinkWrapper
import org.p2p.wallet.svl.model.SendViaLinkClaimingState
import org.p2p.wallet.svl.model.TemporaryAccountState
import org.p2p.wallet.svl.ui.error.SendViaLinkError
import org.p2p.wallet.svl.ui.error.SendViaLinkErrorFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.doOnAnimationEnd
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_ACCOUNT_STATE = "ARG_ACCOUNT_STATE"
private const val ARG_LINK = "ARG_LINK"

class ReceiveViaLinkBottomSheet :
    BaseMvpBottomSheet<ReceiveViaLinkContract.View, ReceiveViaLinkContract.Presenter>(
        layoutRes = R.layout.dialog_send_via_link_receive_funds
    ),
    ReceiveViaLinkContract.View {
    companion object {
        fun show(fm: FragmentManager, state: TemporaryAccountState, link: SendViaLinkWrapper) {
            ReceiveViaLinkBottomSheet()
                .withArgs(
                    ARG_ACCOUNT_STATE to state,
                    ARG_LINK to link
                )
                .show(fm, ReceiveViaLinkBottomSheet::javaClass.name)
        }
    }

    private val state: TemporaryAccountState by args(ARG_ACCOUNT_STATE)
    private val link: SendViaLinkWrapper by args(ARG_LINK)

    private val binding: DialogSendViaLinkReceiveFundsBinding by viewBinding()

    private var listener: RootListener? = null

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override val presenter: ReceiveViaLinkContract.Presenter by inject()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? RootListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        UiKitDrawableCellModels.shapeCircleWithTint(R.color.bg_rain)
            .applyBackground(binding.layoutClaimSuccess.imageViewIcon)

        presenter.handleState(state)
    }

    override fun renderClaimTokenDetails(
        tokenAmount: TextViewCellModel,
        sentFromAddress: TextViewCellModel,
        tokenIcon: ImageViewCellModel,
    ) = with(binding) {
        imageViewTokenIcon.bind(tokenIcon)
        textViewTokenAmount.bind(tokenAmount)
    }

    override fun renderState(state: SendViaLinkClaimingState) = with(binding) {
        when (state) {
            is SendViaLinkClaimingState.ReadyToClaim -> {
                textViewTitle.isVisible = true
                textViewSubtitle.isVisible = false
                layoutTransactionDetails.isVisible = true
                progressStateTransaction.isVisible = false
                imageViewBanner.isVisible = false
                buttonDone.setText(R.string.common_confirm)
                buttonDone.setOnClickListener {
                    presenter.claimToken(state.temporaryAccount, state.token)
                }
            }
            is SendViaLinkClaimingState.ClaimingInProcess -> {
                layoutClaimSuccess.root.isVisible = false
                progressStateTransaction.isVisible = true
                textViewTitle.isVisible = true
                textViewSubtitle.isVisible = false
                layoutTransactionDetails.isVisible = true
                imageViewBanner.isVisible = false
                progressStateTransaction.setDescriptionText(R.string.transaction_description_progress)
                buttonDone.setText(R.string.common_close)
                buttonDone.setOnClickListener { dismissAllowingStateLoss() }
            }
            is SendViaLinkClaimingState.ClaimSuccess -> {
                layoutClaimSuccess.root.isVisible = true
                textViewTitle.isVisible = false
                textViewSubtitle.isVisible = false
                layoutTransactionDetails.isVisible = false
                progressStateTransaction.isVisible = false
                imageViewBanner.isVisible = false
                layoutClaimSuccess.textViewTitle.bind(state.successMessage)
                playApplauseAnimation()
                buttonDone.setText(R.string.send_via_link_receive_funds_success_button)
                buttonDone.setOnClickListener { dismissAllowingStateLoss() }
            }
            is SendViaLinkClaimingState.ClaimFailed -> {
                textViewTitle.isVisible = true
                textViewSubtitle.isVisible = false
                layoutTransactionDetails.isVisible = true
                progressStateTransaction.isVisible = true
                imageViewBanner.isVisible = false
                progressStateTransaction.setDescriptionText(R.string.transaction_description_failed)
                progressStateTransaction.setFailedState()
                buttonDone.setText(R.string.common_close)
                buttonDone.setOnClickListener { dismissAllowingStateLoss() }
            }
            is SendViaLinkClaimingState.ParsingFailed -> {
                textViewTitle.isVisible = true
                textViewSubtitle.isVisible = true
                textViewTitle.setText(R.string.send_via_link_error_failed_parsing_title)
                textViewSubtitle.setText(R.string.send_via_link_error_failed_parsing_subtitle)

                imageViewBanner.isVisible = true
                progressStateTransaction.isVisible = false
                layoutClaimSuccess.root.isVisible = false

                buttonDone.setText(R.string.common_reload)
                buttonDone.setOnClickListener {
                    listener?.parseTransferViaLink(link)
                    dismissAllowingStateLoss()
                }
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
