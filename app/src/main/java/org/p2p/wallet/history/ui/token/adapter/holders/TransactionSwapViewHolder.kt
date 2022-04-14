package org.p2p.wallet.history.ui.token.adapter.holders

import android.annotation.SuppressLint
import android.graphics.drawable.PictureDrawable
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.wallet.R
import org.p2p.wallet.common.date.toTimeString
import org.p2p.wallet.common.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.databinding.ItemTransactionSwapBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.utils.dip
import org.p2p.wallet.utils.viewbinding.context
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone

private const val IMAGE_SIZE = 24

class TransactionSwapViewHolder(
    parent: ViewGroup,
    private val onTransactionClicked: (HistoryTransaction) -> Unit,
    private val binding: ItemTransactionSwapBinding = parent.inflateViewBinding(attachToRoot = false),
) : HistoryTransactionViewHolder(binding.root) {

    private val requestBuilder: RequestBuilder<PictureDrawable> =
        Glide.with(binding.root.context)
            .`as`(PictureDrawable::class.java)
            .listener(SvgSoftwareLayerSetter())

    fun onBind(item: HistoryItem.TransactionItem) {
        when (item.transaction) {
            is HistoryTransaction.Swap -> showSwapTransaction(item.transaction)
        }
        itemView.setOnClickListener { onTransactionClicked(item.transaction) }
    }

    @SuppressLint("SetTextI18n")
    private fun showSwapTransaction(transaction: HistoryTransaction.Swap) {
        with(binding) {
            loadImage(sourceImageView, transaction.sourceIconUrl)
            loadImage(destinationImageView, transaction.destinationIconUrl)

            with(transactionData) {
                addressTextView.text = "${transaction.sourceSymbol} to ${transaction.destinationSymbol}"
                valueTextView withTextOrGone transaction.getReceivedUsdAmount()
                totalTextView.text = "+ ${transaction.amountB} ${transaction.destinationSymbol}"
                totalTextView.setTextColor(valueTextView.context.getColor(R.color.colorGreen))
                timeTextView.text = transaction.date.toTimeString()
            }
        }
    }

    private fun loadImage(imageView: ImageView, url: String) {
        if (".svg" in url) {
            val size = binding.context.dip(IMAGE_SIZE)
            requestBuilder.load(url)
                .apply(RequestOptions().override(size, size))
                .centerCrop()
                .into(imageView)
        } else {
            Glide.with(imageView).load(url).into(imageView)
        }
    }
}
