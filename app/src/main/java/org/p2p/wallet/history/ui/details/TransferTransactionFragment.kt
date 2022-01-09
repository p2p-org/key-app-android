package org.p2p.wallet.history.ui.details

import android.annotation.SuppressLint
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.wallet.R
import org.p2p.wallet.common.date.toDateTimeString
import org.p2p.wallet.common.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentTransactionTransferBinding
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransferType
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.dip
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.p2p.wallet.utils.withTextOrGone

class TransferTransactionFragment : BaseFragment(R.layout.fragment_transaction_transfer) {

    companion object {
        private const val EXTRA_TRANSACTION = "EXTRA_TRANSACTION"
        private const val IMAGE_SIZE = 24

        fun create(transaction: HistoryTransaction) =
            TransferTransactionFragment()
                .withArgs(EXTRA_TRANSACTION to transaction)
    }

    private val transaction: HistoryTransaction.Transfer by args(EXTRA_TRANSACTION)

    private val binding: FragmentTransactionTransferBinding by viewBinding()

    private lateinit var requestBuilder: RequestBuilder<PictureDrawable>

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                detailsButton.fitMargin { Edge.BottomArc }
            }

            requestBuilder = Glide.with(requireContext())
                .`as`(PictureDrawable::class.java)
                .listener(SvgSoftwareLayerSetter())

            val isSend = transaction.type == TransferType.SEND

            toolbar.setTitle(if (isSend) R.string.main_transfer else R.string.main_receive)
            toolbar.setNavigationOnClickListener { popBackStack() }
            toolbar.subtitle = transaction.date.toDateTimeString()

            if (isSend) {
                loadImage(sourceImageView, transaction.tokenData.iconUrl.orEmpty())
            } else {
                loadImage(destinationImageView, transaction.tokenData.iconUrl.orEmpty())
            }

            totalTextView.text = transaction.getTotal()
            usdAmountTextView withTextOrGone transaction.getValue()

            sourceSymbolTextView.text = transaction.tokenData.symbol
            destinationSymbolTextView.text = transaction.tokenData.symbol

            sourceAddressTextView.text = transaction.senderAddress
            destinationAddressTextView.text = transaction.destination

            sourceViewImageView.setOnClickListener {
                requireContext().copyToClipBoard(transaction.senderAddress)
                Toast.makeText(requireContext(), R.string.common_copied, Toast.LENGTH_SHORT).show()
            }

            destinationViewImageView.setOnClickListener {
                requireContext().copyToClipBoard(transaction.destination)
                Toast.makeText(requireContext(), R.string.common_copied, Toast.LENGTH_SHORT).show()
            }

            amountTextView.text = transaction.getFormattedTotal()
            valueTextView.text = transaction.getFormattedAmount()

            paidByTextView.isVisible = isSend
            feeTextView.text = "${transaction.fee} lamports"
            blockNumberTextView.text = "#${transaction.blockNumber}"
            transactionIdTextView.text = transaction.signature
            viewImageView.setOnClickListener {
                val url = getString(R.string.solanaExplorer, transaction.signature)
                showUrlInCustomTabs(url)
            }

            detailsButton.setOnClickListener {
                val isVisible = amountDivider.isVisible

                amountDivider.isVisible = !isVisible
                amountTitleTextView.isVisible = !isVisible
                amountTextView.isVisible = !isVisible

                valueDivider.isVisible = !isVisible
                valueTitleTextView.isVisible = !isVisible
                valueTextView.isVisible = !isVisible

                feeDivider.isVisible = !isVisible
                feeView.isVisible = !isVisible
                feeTextView.isVisible = !isVisible

                blockNumberDivider.isVisible = !isVisible
                blockNumberTitleTextView.isVisible = !isVisible
                blockNumberTextView.isVisible = !isVisible

                val resId = if (isVisible) {
                    R.string.details_show_transaction_details
                } else {
                    R.string.details_hide_transaction_details
                }
                detailsButton.setText(resId)

                TransitionManager.beginDelayedTransition(detailsView)
            }
        }
    }

    private fun loadImage(imageView: ImageView, url: String) {
        if (url.contains(".svg")) {
            val size = dip(IMAGE_SIZE)
            requestBuilder.load(url)
                .apply(RequestOptions().override(size, size))
                .centerCrop()
                .into(imageView)
        } else {
            Glide.with(imageView).load(url).into(imageView)
        }
    }
}