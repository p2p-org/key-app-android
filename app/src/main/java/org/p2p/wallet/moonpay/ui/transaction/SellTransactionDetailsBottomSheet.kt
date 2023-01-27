package org.p2p.wallet.moonpay.ui.transaction

import androidx.annotation.ColorRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.utils.removeLinksUnderline
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.common.date.isSameDayAs
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogSendTransactionDetailsBinding
import org.p2p.wallet.home.ui.select.bottomsheet.SelectTokenBottomSheet
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.p2p.wallet.newsend.ui.NewSendFragment
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.getColorStateListCompat
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.context
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.getHtmlString
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.math.BigDecimal
import java.util.*

private const val ARG_DETAILS = "ARG_DETAILS"
private const val DATE_FORMAT = "MMMM dd, yyyy"
private const val TIME_FORMAT = "HH:mm"

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

    private val dateFormat by unsafeLazy { DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.US) }
    private val timeFormat by unsafeLazy { DateTimeFormatter.ofPattern(TIME_FORMAT, Locale.US) }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun onActionButtonClicked(action: SellTransactionDetailsButtonAction) {
        when (action) {
            SellTransactionDetailsButtonAction.SEND -> {
                presenter.onSendClicked()
            }
            SellTransactionDetailsButtonAction.CLOSE -> {
                close()
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
        val bodyBackgroundTint: Int
        val bodyTextColorRes: Int = if (isFailedTransaction) R.color.text_rose else R.color.text_night
        val bodyIconRes: Int
        val bodyIconTint: Int
        val buttonTitle: String
        val buttonRemoveOrCancelTitle: String
        val action: SellTransactionDetailsButtonAction
        when (details.status) {
            SellTransactionStatus.WAITING_FOR_DEPOSIT -> {
                title = getString(R.string.sell_details_waiting_deposit_title, details.formattedSolAmount)
                body = getString(R.string.sell_details_waiting_deposit_body)
                bodyBackgroundTint = R.color.bg_rain
                bodyIconRes = R.drawable.ic_alert_rounded
                bodyIconTint = R.color.icons_sun
                buttonTitle = getString(R.string.common_send)
                buttonRemoveOrCancelTitle = getString(R.string.sell_lock_cancel_transaction)
                action = SellTransactionDetailsButtonAction.SEND
            }
            SellTransactionStatus.PENDING -> {
                title = getString(R.string.sell_details_pending_title)
                body = getHtmlString(R.string.sell_details_pending_body).removeLinksUnderline()
                bodyBackgroundTint = R.color.light_silver
                bodyIconRes = R.drawable.ic_info_rounded
                bodyIconTint = R.color.icons_silver
                buttonTitle = getString(R.string.common_close)
                buttonRemoveOrCancelTitle = emptyString()
                action = SellTransactionDetailsButtonAction.CLOSE
            }
            SellTransactionStatus.COMPLETED -> {
                title = getString(R.string.sell_details_completed_title)
                body = getHtmlString(R.string.sell_details_completed_body).removeLinksUnderline()
                bodyBackgroundTint = R.color.light_silver
                bodyIconRes = R.drawable.ic_info_rounded
                bodyIconTint = R.color.icons_silver
                buttonTitle = getString(R.string.common_close)
                buttonRemoveOrCancelTitle = getString(R.string.sell_details_button_remove)
                action = SellTransactionDetailsButtonAction.CLOSE
            }
            SellTransactionStatus.FAILED -> {
                title = getString(R.string.sell_details_failed_title, details.formattedSolAmount)
                body = getString(R.string.sell_details_failed_body)
                bodyBackgroundTint = R.color.rose_20
                bodyIconRes = R.drawable.ic_alert_rounded
                bodyIconTint = R.color.icons_rose
                buttonTitle = getString(R.string.common_try_again)
                buttonRemoveOrCancelTitle = getString(R.string.sell_details_button_delete)
                action = SellTransactionDetailsButtonAction.TRY_AGAIN
            }
        }

        setupTitleAndBody(
            title = title,
            body = body,
            bodyIcon = bodyIconRes,
            bodyTextColorRes = bodyTextColorRes,
            bodyIconTint = ColorStateList.valueOf(getColor(bodyIconTint)),
            bodyBackground = bodyBackgroundTint
        )
        setupTransactionDate(details.updatedAt)

        setupButtons(
            action = action,
            buttonTitle = buttonTitle,
            buttonRemoveOrCancelTitle = buttonRemoveOrCancelTitle
        )
    }

    private fun setupTransactionDate(
        updatedAt: String?
    ) = with(binding.layoutDetails) {
        textViewSubtitle.isVisible = !updatedAt.isNullOrEmpty()
        val time = updatedAt?.toZonedDateTime() ?: return@with
        val now = ZonedDateTime.now()
        val isToday = time.isSameDayAs(now)
        val firstPart = if (isToday) getString(R.string.common_today) else dateFormat.format(time)
        textViewSubtitle.text = getString(
            R.string.transaction_date_format,
            firstPart,
            timeFormat.format(time)
        )
    }

    private fun setupTitleAndBody(
        title: String,
        body: CharSequence, // can be spanned link
        bodyIcon: Int,
        bodyIconTint: ColorStateList,
        @ColorRes bodyBackground: Int,
        bodyTextColorRes: Int
    ) = with(binding.layoutDetails) {
        textViewTitle.text = title
        textViewTitle.setTextAppearance(R.style.UiKit_TextAppearance_SemiBold_Text1)
        textViewMessageBody.text = body
        textViewMessageBody.setTextColorRes(bodyTextColorRes)
        textViewMessageBody.setLinkTextColor(getColor(R.color.text_sky))
        textViewMessageBody.movementMethod = LinkMovementMethod.getInstance()
        imageViewMessageIcon.setImageResource(bodyIcon)
        imageViewMessageIcon.imageTintList = bodyIconTint

        containerMessage.backgroundTintList = context.getColorStateListCompat(bodyBackground)
    }

    private fun setupButtons(
        action: SellTransactionDetailsButtonAction,
        buttonTitle: String,
        buttonRemoveOrCancelTitle: String
    ) = with(binding.layoutDetails) {
        // temp remove try again button due to absence of implementation
        buttonAction.isVisible = details.status != SellTransactionStatus.FAILED
        buttonAction.text = buttonTitle
        buttonAction.setOnClickListener { onActionButtonClicked(action) }

        buttonRemoveOrCancel.text = buttonRemoveOrCancelTitle
        buttonRemoveOrCancel.isVisible = details.status == SellTransactionStatus.WAITING_FOR_DEPOSIT ||
            details.status == SellTransactionStatus.FAILED ||
            details.status == SellTransactionStatus.COMPLETED
        buttonRemoveOrCancel.setOnClickListener {
            when (details.status) {
                SellTransactionStatus.WAITING_FOR_DEPOSIT -> presenter.onCancelTransactionClicked()
                SellTransactionStatus.FAILED,
                SellTransactionStatus.COMPLETED ->
                    presenter.onRemoveFromHistoryClicked()
                else -> Unit
            }
        }
    }

    private fun renderAmounts() = with(binding.layoutDetails) {
        val tokenAmount = details.formattedSolAmount
        val fiatAmount = details.formattedFiatAmount
        val fiatAbbreviation = details.fiatAbbreviation
        val boldAmount: String
        val labelAmount: String
        val receiverAddress: String
        val receiverTitle: String = getString(
            if (details.status == SellTransactionStatus.PENDING) {
                R.string.sell_details_will_be_send_to
            } else {
                R.string.main_send_to
            }
        )
        when (details.status) {
            SellTransactionStatus.WAITING_FOR_DEPOSIT -> {
                boldAmount = getString(
                    R.string.sell_lock_token_amount, tokenAmount, Constants.SOL_SYMBOL
                )
                labelAmount = emptyString()
                receiverAddress = details.receiverAddress.let {
                    if (details.isReceiverAddressWallet) it.cutMiddle() else it
                }
            }
            SellTransactionStatus.PENDING -> {
                boldAmount = getString(
                    R.string.sell_lock_pending_fiat_amount, tokenAmount, Constants.SOL_SYMBOL,
                )
                labelAmount = emptyString()
                receiverAddress = getString(R.string.sell_details_receiver_moonpay_bank)
            }

            SellTransactionStatus.COMPLETED -> {
                boldAmount = getString(
                    R.string.sell_lock_token_amount, fiatAmount, fiatAbbreviation,
                )
                labelAmount = getString(
                    R.string.sell_lock_token_amount, tokenAmount, Constants.SOL_SYMBOL
                )
                receiverAddress = getString(R.string.sell_details_receiver_moonpay_bank)
            }
            SellTransactionStatus.FAILED -> {
                boldAmount = getString(
                    R.string.sell_lock_token_amount, tokenAmount, Constants.SOL_SYMBOL
                )
                labelAmount = emptyString()
                receiverAddress = emptyString()
            }
        }
        textViewAmount.text = boldAmount
        textViewFiatValue.text = labelAmount
        textViewFiatValue.isVisible = labelAmount.isNotEmpty()
        textViewReceiverTitle.text = receiverTitle

        containerReceiver.isVisible = details.status != SellTransactionStatus.FAILED
        textViewReceiverAddress.text = receiverAddress
    }

    private fun renderCopyButton() = with(binding.layoutDetails.imageViewCopy) {
        isVisible = when (details.status) {
            SellTransactionStatus.WAITING_FOR_DEPOSIT -> details.isReceiverAddressWallet
            SellTransactionStatus.PENDING,
            SellTransactionStatus.FAILED,
            SellTransactionStatus.COMPLETED -> false
        }
        setOnClickListener {
            requireContext().copyToClipBoard(details.receiverAddress)
            showUiKitSnackBar(messageResId = R.string.common_copied)
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
                inputAmount = sendAmount
            )
        )
        dismissAllowingStateLoss()
    }

    override fun close() {
        setFragmentResult(REQUEST_KEY_DISMISSED, bundleOf())
        dismissAllowingStateLoss()
    }
}
