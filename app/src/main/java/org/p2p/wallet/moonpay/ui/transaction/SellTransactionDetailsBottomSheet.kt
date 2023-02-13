package org.p2p.wallet.moonpay.ui.transaction

import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.token.Token
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogSendTransactionDetailsBinding
import org.p2p.wallet.home.ui.select.bottomsheet.SelectTokenBottomSheet
import org.p2p.wallet.newsend.model.AddressState
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.ui.NewSendFragment
import org.p2p.wallet.newsend.ui.SendOpenedFrom
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.getColorStateListCompat
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.context
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone
import java.math.BigDecimal

private const val ARG_DETAILS = "ARG_DETAILS"

class SellTransactionDetailsBottomSheet :
    BaseMvpBottomSheet<SellTransactionDetailsContract.View, SellTransactionDetailsContract.Presenter>(
        R.layout.dialog_send_transaction_details
    ),
    SellTransactionDetailsContract.View {

    companion object {
        const val REQUEST_KEY_DISMISSED = "REQUEST_KEY_DISMISSED"

        fun show(fm: FragmentManager, details: SellTransactionViewDetails) {
            SellTransactionDetailsBottomSheet()
                .withArgs(ARG_DETAILS to details)
                .show(fm, SelectTokenBottomSheet::javaClass.name)
        }
    }

    override val presenter: SellTransactionDetailsContract.Presenter by inject { parametersOf(details) }
    private val binding: DialogSendTransactionDetailsBinding by viewBinding()

    private val details: SellTransactionViewDetails by args(ARG_DETAILS)

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun renderViewState(viewState: SellTransactionDetailsViewState) {
        renderTitle(viewState.titleBlock)
        renderMessage(viewState.messageBlock)
        viewState.receiverBlock?.let { renderReceiver(it) }
        renderButtons(viewState.buttonsBlock)
    }

    private fun renderTitle(titleBlock: SellTransactionDetailsViewState.TitleBlock) {
        with(binding.layoutDetails) {
            textViewTitle.text = titleBlock.title
            textViewTitle.setTextAppearance(R.style.UiKit_TextAppearance_SemiBold_Text1)
            textViewSubtitle.isVisible = titleBlock.updatedAt.isNotEmpty()
            textViewSubtitle.text = titleBlock.updatedAt
            textViewAmount.text = titleBlock.boldAmount
            textViewFiatValue.withTextOrGone(titleBlock.labelAmount)
        }
    }

    private fun renderMessage(bodyBlock: SellTransactionDetailsViewState.BodyBlock) {
        with(binding.layoutDetails) {
            textViewMessageBody.text = bodyBlock.bodyText
            textViewMessageBody.setTextColorRes(bodyBlock.bodyTextColor)
            textViewMessageBody.setLinkTextColor(getColor(R.color.text_sky))
            textViewMessageBody.movementMethod = LinkMovementMethod.getInstance()

            imageViewMessageIcon.setImageResource(bodyBlock.bodyIconRes)
            imageViewMessageIcon.imageTintList = ColorStateList.valueOf(getColor(bodyBlock.bodyIconTint))

            containerMessage.backgroundTintList = context.getColorStateListCompat(bodyBlock.bodyBackgroundColor)
        }
    }

    private fun renderReceiver(receiverBlock: SellTransactionDetailsViewState.ReceiverBlock) {
        with(binding.layoutDetails) {
            containerReceiver.isVisible = true
            textViewReceiverTitle.text = receiverBlock.receiverTitle
            textViewReceiverAddress.text = receiverBlock.receiverValue

            imageViewCopy.isVisible = receiverBlock.isCopyEnabled
            receiverBlock.copyValueProvider?.let { copyValueProvider ->
                imageViewCopy.setOnClickListener {
                    requireContext().copyToClipBoard(copyValueProvider.invoke())
                    showUiKitSnackBar(messageResId = R.string.common_copied)
                }
            }
        }
    }

    private fun renderButtons(buttonsBlock: SellTransactionDetailsViewState.ButtonsBlock) {
        with(binding.layoutDetails) {
            if (buttonsBlock.mainButtonTitle != null && buttonsBlock.mainButtonAction != null) {
                buttonAction.isVisible = true
                buttonAction.text = buttonsBlock.mainButtonTitle
                buttonAction.setOnClickListener { buttonsBlock.mainButtonAction.invoke() }
            } else {
                buttonAction.isVisible = false
            }

            if (buttonsBlock.additionalButtonTitle != null && buttonsBlock.additionalButtonAction != null) {
                buttonRemoveOrCancel.isVisible = true
                buttonRemoveOrCancel.text = buttonsBlock.additionalButtonTitle
                buttonRemoveOrCancel.setOnClickListener { buttonsBlock.additionalButtonAction.invoke() }
            } else {
                buttonRemoveOrCancel.isVisible = false
            }
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.layoutDetails.buttonRemoveOrCancel.isLoadingState = isLoading
    }

    override fun navigateToSendScreen(
        tokenToSend: Token.Active,
        sendAmount: BigDecimal,
        receiverAddress: String
    ) {
        val recipient = SearchResult.AddressFound(AddressState(receiverAddress))
        replaceFragment(
            NewSendFragment.create(
                recipient = recipient,
                initialToken = tokenToSend,
                inputAmount = sendAmount,
                openedFrom = SendOpenedFrom.SELL_FLOW
            )
        )
        dismissAllowingStateLoss()
    }

    override fun close() {
        setFragmentResult(REQUEST_KEY_DISMISSED, bundleOf())
        dismissAllowingStateLoss()
    }
}
