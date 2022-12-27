package org.p2p.wallet.moonpay.ui.transaction

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import org.p2p.core.utils.Constants
import org.p2p.core.utils.removeLinksUnderline
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogSendTransactionDetailsBinding
import org.p2p.wallet.home.ui.select.bottomsheet.SelectTokenBottomSheet
import org.p2p.wallet.moonpay.model.MoonpaySellTransaction.SellTransactionStatus
import org.p2p.wallet.newsend.ui.NewSendFragment
import org.p2p.wallet.sell.ui.lock.SellTransactionDetails
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.getHtmlString
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import timber.log.Timber

private const val ARG_DETAILS = "ARG_DETAILS"

class SellTransactionDetailsBottomSheet :
    BaseMvpBottomSheet<SellTransactionDetailsContract.View, SellTransactionDetailsContract.Presenter>(
        R.layout.dialog_send_transaction_details
    ),
    SellTransactionDetailsContract.View {

    companion object {
        fun show(fm: FragmentManager, details: SellTransactionDetails) {
            SellTransactionDetailsBottomSheet()
                .withArgs(ARG_DETAILS to details)
                .show(fm, SelectTokenBottomSheet::javaClass.name)
        }
    }

    override val presenter: SellTransactionDetailsContract.Presenter = SellTransactionDetailsPresenter()
    private val binding: DialogSendTransactionDetailsBinding by viewBinding()

    private val details: SellTransactionDetails by args(ARG_DETAILS)

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        expandToFitAllContent()
    }

    private fun onActionButtonClicked(action: SellTransactionDetailsButtonAction) {
        when (action) {
            SellTransactionDetailsButtonAction.SEND -> {
                val recipient = SearchResult.AddressFound(AddressState(details.receiverAddress))
                replaceFragment(NewSendFragment.create(recipient = recipient))
            }
            SellTransactionDetailsButtonAction.CLOSE -> {
                dismissAllowingStateLoss()
            }
            SellTransactionDetailsButtonAction.TRY_AGAIN -> {
                // todo should be a task somewhere to implement try again feature
            }
        }
    }

    private fun setupViews() = with(binding.layoutDetails) {
        renderAmounts()
        renderCopyButton()

        val isFailedTransaction = details.status == SellTransactionStatus.FAILED
        val title: String
        val body: CharSequence
        val bodyBackground: Int
        val bodyTextColorRes: Int = if (isFailedTransaction) R.color.text_rose else R.color.text_night
        val bodyIconRes: Int
        val bodyIconTint: Int
        val buttonTitle: String
        val action: SellTransactionDetailsButtonAction
        when (details.status) {
            SellTransactionStatus.WAITING_FOR_DEPOSIT -> {
                title = getString(R.string.sell_details_waiting_deposit_title, details.formattedSolAmount)
                body = getString(R.string.sell_details_waiting_deposit_body)
                bodyBackground = R.drawable.bg_rounded_solid_rain_24
                bodyIconRes = R.drawable.ic_alert_rounded
                bodyIconTint = R.color.icons_sun
                buttonTitle = getString(R.string.common_send)
                action = SellTransactionDetailsButtonAction.SEND
            }
            SellTransactionStatus.PENDING -> {
                title = getString(R.string.sell_details_pending_title)
                body = getHtmlString(R.string.sell_details_pending_body).removeLinksUnderline()
                bodyBackground = R.drawable.bg_rounded_solid_rain_24
                bodyIconRes = R.drawable.ic_info_rounded
                bodyIconTint = R.color.icons_silver
                buttonTitle = getString(R.string.common_close)
                action = SellTransactionDetailsButtonAction.CLOSE
            }
            SellTransactionStatus.COMPLETED -> {
                title = getString(R.string.sell_details_completed_title)
                body = getHtmlString(R.string.sell_details_completed_body).removeLinksUnderline()
                bodyBackground = R.drawable.bg_rounded_solid_rain_24
                bodyIconRes = R.drawable.ic_info_rounded
                bodyIconTint = R.color.icons_silver
                buttonTitle = getString(R.string.common_close)
                action = SellTransactionDetailsButtonAction.CLOSE
            }
            SellTransactionStatus.FAILED -> {
                title = getString(R.string.sell_details_failed_title, details.formattedSolAmount)
                body = getString(R.string.sell_details_failed_body)
                bodyBackground = R.drawable.bg_rounded_solid_rose20_12
                bodyIconRes = R.drawable.ic_alert_rounded
                bodyIconTint = R.color.icons_rose
                buttonTitle = getString(R.string.common_try_again)
                action = SellTransactionDetailsButtonAction.TRY_AGAIN
            }
            SellTransactionStatus.UNKNOWN -> {
                Timber.e(IllegalArgumentException("Can't render unknown transaction"))
                return
            }
        }

        setupTitleAndBody(
            title = title,
            body = body,
            bodyIcon = bodyIconRes,
            bodyTextColorRes = bodyTextColorRes,
            bodyIconTint = ColorStateList.valueOf(getColor(bodyIconTint)),
            bodyBackground = bodyBackground
        )

        setupButtons(
            action = action,
            buttonTitle = buttonTitle
        )
    }

    private fun setupTitleAndBody(
        title: String,
        body: CharSequence, // can be spanned link
        bodyIcon: Int,
        bodyIconTint: ColorStateList,
        bodyBackground: Int,
        bodyTextColorRes: Int
    ) = with(binding.layoutDetails) {
        textViewTitle.text = title
        textViewMessageBody.text = body
        textViewMessageBody.setTextColorRes(bodyTextColorRes)
        textViewMessageBody.setLinkTextColor(getColor(R.color.text_sky))
        textViewMessageBody.movementMethod = LinkMovementMethod.getInstance()
        imageViewMessageIcon.setImageResource(bodyIcon)
        imageViewMessageIcon.imageTintList = bodyIconTint

        containerMessage.setBackgroundResource(bodyBackground)
    }

    private fun setupButtons(
        action: SellTransactionDetailsButtonAction,
        buttonTitle: String,
    ) = with(binding.layoutDetails) {
        buttonAction.text = buttonTitle
        buttonAction.setOnClickListener { onActionButtonClicked(action) }
        buttonRemove.isVisible =
            details.status == SellTransactionStatus.WAITING_FOR_DEPOSIT ||
            details.status == SellTransactionStatus.FAILED
        buttonRemove.setOnClickListener { presenter.removeFromHistory() }
    }

    private fun renderAmounts() = with(binding.layoutDetails) {
        val solAmount = details.formattedSolAmount
        val usdAmount = details.formattedUsdAmount
        textViewAmount.text = "$solAmount ${Constants.SOL_SYMBOL}"
        textViewUsdValue.text = getString(R.string.sell_lock_usd_amount, usdAmount)

        textViewReceiverAddress.text = details.receiverAddress.let {
            if (details.isReceiverAddressWallet) it.cutMiddle() else it
        }
    }

    private fun renderCopyButton() = with(binding.layoutDetails.imageViewCopy) {
        isVisible = details.isReceiverAddressWallet
        setOnClickListener {
            requireContext().copyToClipBoard(details.receiverAddress)
            showUiKitSnackBar(messageResId = R.string.common_copied)
        }
    }
}
