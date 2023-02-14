package org.p2p.wallet.sell.ui.lock

import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.utils.insets.appleBottomInsets
import org.p2p.core.utils.insets.appleTopInsets
import org.p2p.core.utils.insets.consume
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSellLockBinding
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.p2p.wallet.newsend.model.AddressState
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.ui.NewSendFragment
import org.p2p.wallet.newsend.ui.SendOpenedFrom
import org.p2p.wallet.sell.analytics.SellAnalytics
import org.p2p.wallet.sell.ui.payload.SellPayloadFragment
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.toPx
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.math.BigDecimal

private const val ARG_SELL_LOCKED = "ARG_SELL_LOCKED"
private const val ARG_IS_TRANSACTION_JUST_CREATED = "ARG_IS_TRANSACTION_JUST_CREATED"

class SellLockedFragment :
    BaseMvpFragment<SellLockedContract.View, SellLockedContract.Presenter>(R.layout.fragment_sell_lock),
    SellLockedContract.View {

    companion object {
        fun create(details: SellTransactionViewDetails, isTransactionJustCreated: Boolean): SellLockedFragment {
            require(details.status == SellTransactionStatus.WAITING_FOR_DEPOSIT) {
                "This fragment is used only if status == waiting for deposit"
            }
            return SellLockedFragment()
                .withArgs(
                    ARG_SELL_LOCKED to details,
                    ARG_IS_TRANSACTION_JUST_CREATED to isTransactionJustCreated
                )
        }
    }

    override val presenter: SellLockedContract.Presenter by inject { parametersOf(details) }

    private val binding: FragmentSellLockBinding by viewBinding()
    private val details: SellTransactionViewDetails by args(ARG_SELL_LOCKED)
    private val isTransactionJustCreated: Boolean by args(ARG_IS_TRANSACTION_JUST_CREATED)
    private val sellAnalytics: SellAnalytics by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            if (isTransactionJustCreated) showWarningDialog() else navigateBackToMain()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (isTransactionJustCreated) showWarningDialog() else navigateBackToMain()
        }

        setupViews()

        sellAnalytics.logSellLockedOpened()
    }

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { _, insets, _ ->
            insets.systemAndIme().consume {
                binding.toolbar.appleTopInsets(this)
                binding.layoutDetails.root.appleBottomInsets(this)
            }
        }
    }

    private fun setupViews() = with(binding.layoutDetails) {
        setupLayoutDetails()
        renderAmounts()
        renderCopyButton()
        setupTitleAndBody()

        setupButtons()
    }

    private fun setupLayoutDetails() = with(binding.layoutDetails) {
        root.setBackgroundColor(getColor(R.color.bg_smoke))
        root.updatePadding(top = 16.toPx())
    }

    private fun setupTitleAndBody() = with(binding.layoutDetails) {
        val title = getString(R.string.sell_lock_title)
        val body = getString(R.string.sell_details_waiting_deposit_body)
        val bodyBackground = R.drawable.bg_rounded_solid_rain_24
        val bodyTextColorRes: Int = R.color.text_night
        val bodyIconRes = R.drawable.ic_alert_rounded
        val bodyIconTint = R.color.icons_sun

        textViewTitle.text = title
        textViewMessageBody.text = body
        textViewMessageBody.setTextColorRes(bodyTextColorRes)
        textViewMessageBody.setLinkTextColor(getColor(R.color.text_sky))
        textViewMessageBody.movementMethod = LinkMovementMethod.getInstance()

        imageViewMessageIcon.setImageResource(bodyIconRes)
        imageViewMessageIcon.imageTintList = ColorStateList.valueOf(getColor(bodyIconTint))

        containerMessage.setBackgroundResource(bodyBackground)
    }

    private fun setupButtons() = with(binding.layoutDetails) {
        buttonAction.setText(R.string.sell_details_button_send)
        buttonAction.setOnClickListener { presenter.onSendClicked() }

        buttonRemoveOrCancel.setText(R.string.sell_details_button_cancel)
        buttonRemoveOrCancel.isVisible = !isTransactionJustCreated
        buttonRemoveOrCancel.setOnClickListener { presenter.onCancelTransactionClicked() }
    }

    private fun renderAmounts() = with(binding.layoutDetails) {
        val solAmount = details.formattedSolAmount

        textViewAmount.text = getString(
            R.string.sell_details_token_amount, solAmount, Constants.SOL_SYMBOL
        )
        textViewFiatValue.isVisible = false

        textViewReceiverTitle.setText(R.string.sell_details_send_to)
        textViewReceiverAddress.text = details.receiverAddress.cutMiddle(cutCount = CUT_ADDRESS_SYMBOLS_COUNT)
    }

    private fun renderCopyButton() = with(binding.layoutDetails.imageViewCopy) {
        isVisible = details.isReceiverAddressWallet
        binding.layoutDetails.containerReceiver.isVisible = true
        setOnClickListener {
            requireContext().copyToClipBoard(details.receiverAddress)
            showUiKitSnackBar(messageResId = R.string.common_copied)
        }
    }

    override fun navigateBack() {
        popBackStackTo(SellPayloadFragment::class)
    }

    override fun navigateBackToMain() {
        popBackStackTo(MainFragment::class)
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
    }

    private fun showWarningDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setIcon(R.drawable.ic_key_app_circle)
            .setTitle(getString(R.string.sell_lock_warning_dialog_title))
            .setMessage(getString(R.string.sell_lock_warning_dialog_body))
            .setPositiveButton(R.string.sell_lock_warning_dialog_positive) { dialog, _ -> dialog.dismiss() }
            .setNegativeButton(R.string.sell_lock_warning_dialog_negative) { _, _ -> navigateBackToMain() }
            .show()
    }
}
