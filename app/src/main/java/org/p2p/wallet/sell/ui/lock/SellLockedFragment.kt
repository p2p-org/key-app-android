package org.p2p.wallet.sell.ui.lock

import androidx.core.view.isVisible
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.utils.Constants
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSellLockBinding
import org.p2p.wallet.moonpay.model.MoonpaySellTransaction.SellTransactionStatus
import org.p2p.wallet.newsend.ui.NewSendFragment
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_SELL_LOCKED = "ARG_SELL_LOCKED"

class SellLockedFragment :
    BaseMvpFragment<SellLockedContract.View, SellLockedContract.Presenter>(R.layout.fragment_sell_lock),
    SellLockedContract.View {

    companion object {
        fun create(details: SellTransactionDetails): SellLockedFragment {
            require(details.status == SellTransactionStatus.WAITING_FOR_DEPOSIT) {
                "This fragment is used only if status == waiting for deposit"
            }
            return SellLockedFragment()
                .withArgs(ARG_SELL_LOCKED to details)
        }
    }

    override val presenter: SellLockedContract.Presenter by inject()

    private val binding: FragmentSellLockBinding by viewBinding()
    private val details: SellTransactionDetails by args(ARG_SELL_LOCKED)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }

        setupViews()
    }

    private fun setupViews() = with(binding.layoutDetails) {
        renderAmounts()
        renderCopyButton()

        val title = getString(R.string.sell_details_waiting_deposit_title, details.formattedSolAmount)
        val body = getString(R.string.sell_details_waiting_deposit_body)
        val bodyBackground = R.drawable.bg_rounded_solid_rain_24
        val bodyTextColorRes: Int = R.color.text_night
        val bodyIconRes = R.drawable.ic_alert_rounded
        val bodyIconTint = R.color.icons_sun
        val buttonTitle = getString(R.string.common_send)

        setupTitleAndBody(
            title = title,
            body = body,
            bodyIcon = bodyIconRes,
            bodyTextColorRes = bodyTextColorRes,
            bodyIconTint = ColorStateList.valueOf(getColor(bodyIconTint)),
            bodyBackground = bodyBackground
        )

        setupButtons(buttonTitle = buttonTitle)
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

    private fun setupButtons(buttonTitle: String) = with(binding.layoutDetails) {
        buttonAction.text = buttonTitle
        buttonAction.setOnClickListener {
            val recipient = SearchResult.AddressOnly(
                AddressState(details.receiverAddress, NetworkType.SOLANA)
            )
            replaceFragment(NewSendFragment.create(recipient = recipient))
        }
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
