package org.p2p.wallet.history.ui.details

import android.annotation.SuppressLint
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.wallet.R
import org.p2p.wallet.common.date.toDateTimeString
import org.p2p.wallet.common.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentTransactionSwapBinding
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.dip
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class SwapTransactionFragment : BaseFragment(R.layout.fragment_transaction_swap) {

    companion object {
        private const val EXTRA_TRANSACTION = "EXTRA_TRANSACTION"
        private const val IMAGE_SIZE = 24

        fun create(transaction: HistoryTransaction.Swap) =
            SwapTransactionFragment()
                .withArgs(EXTRA_TRANSACTION to transaction)
    }

    private val transaction: HistoryTransaction.Swap by args(EXTRA_TRANSACTION)

    private val binding: FragmentTransactionSwapBinding by viewBinding()

    private lateinit var requestBuilder: RequestBuilder<PictureDrawable>

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestBuilder = Glide.with(requireContext())
            .`as`(PictureDrawable::class.java)
            .listener(SvgSoftwareLayerSetter())

        with(binding) {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                detailsButton.fitMargin { Edge.BottomArc }
            }

            toolbar.setNavigationOnClickListener { popBackStack() }
            toolbar.subtitle = transaction.date.toDateTimeString()
            loadImage(sourceImageView, transaction.sourceTokenUrl)
            loadImage(destinationImageView, transaction.destinationTokenUrl)
            sourceTextView.text = "- ${transaction.amountA}"
            sourceSymbolTextView.text = transaction.sourceSymbol
            destinationTextView.text = "+ ${transaction.amountB}"
            destinationSymbolTextView.text = transaction.destinationSymbol

            fromTextView.text = transaction.sourceAddress
            toTextView.text = transaction.destinationAddress
            amountTextView.text = transaction.getFormattedAmount()
            feeTextView.text = transaction.getFormattedFee()
            blockNumberTextView.text = "#${transaction.blockNumber}"
            transactionIdTextView.text = transaction.signature
            viewImageView.setOnClickListener {
                val url = getString(R.string.solanaExplorer, transaction.signature)
                showUrlInCustomTabs(url)
            }

            detailsButton.setOnClickListener {
                val isVisible = sourceDivider.isVisible

                sourceDivider.isVisible = !isVisible
                fromTitleTextView.isVisible = !isVisible
                fromTextView.isVisible = !isVisible

                destinationDivider.isVisible = !isVisible
                toTitleTextView.isVisible = !isVisible
                toTextView.isVisible = !isVisible

                amountDivider.isVisible = !isVisible
                amountTitleTextView.isVisible = !isVisible
                amountTextView.isVisible = !isVisible

                feeDivider.isVisible = !isVisible
                feeTitleTextView.isVisible = !isVisible
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