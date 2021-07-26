package com.p2p.wallet.history.ui.details

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
import com.p2p.wallet.R
import com.p2p.wallet.common.date.toDateTimeString
import com.p2p.wallet.common.glide.SvgSoftwareLayerSetter
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentTransactionSwapBinding
import com.p2p.wallet.history.model.Transaction
import com.p2p.wallet.utils.args
import com.p2p.wallet.utils.dip
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.showUrlInCustomTabs
import com.p2p.wallet.utils.viewbinding.viewBinding
import com.p2p.wallet.utils.withArgs

class SwapTransactionFragment : BaseFragment(R.layout.fragment_transaction_swap) {

    companion object {
        private const val EXTRA_TRANSACTION = "EXTRA_TRANSACTION"
        private const val IMAGE_SIZE = 24

        fun create(transaction: Transaction.Swap) =
            SwapTransactionFragment()
                .withArgs(EXTRA_TRANSACTION to transaction)
    }

    private val transaction: Transaction.Swap by args(EXTRA_TRANSACTION)

    private val binding: FragmentTransactionSwapBinding by viewBinding()

    private lateinit var requestBuilder: RequestBuilder<PictureDrawable>

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestBuilder = Glide.with(requireContext())
            .`as`(PictureDrawable::class.java)
            .listener(SvgSoftwareLayerSetter())

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            toolbar.subtitle = transaction.date.toDateTimeString()
            loadImage(sourceImageView, transaction.sourceTokenUrl)
            loadImage(destinationImageView, transaction.destinationTokenUrl)
            sourceTextView.text = "- ${transaction.amountA} ${transaction.sourceSymbol}"
            destinationTextView.text = "+ ${transaction.amountB} ${transaction.destinationSymbol}"

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
                    R.string.main_receive_show_details
                } else {
                    R.string.main_receive_hide_details
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