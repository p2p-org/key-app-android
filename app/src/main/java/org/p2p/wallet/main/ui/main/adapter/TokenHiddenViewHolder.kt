package org.p2p.wallet.main.ui.main.adapter

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.wallet.common.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.databinding.ItemTokenHiddenBinding
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.model.TokenItem
import org.p2p.wallet.main.model.VisibilityState

class TokenHiddenViewHolder(
    binding: ItemTokenHiddenBinding,
    private val onItemClicked: (Token.Active) -> Unit,
    private val onEditClicked: (Token.Active) -> Unit,
    private val onDeleteClicked: (Token.Active) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        onItemClicked: (Token.Active) -> Unit,
        onEditClicked: (Token.Active) -> Unit,
        onDeleteClicked: (Token.Active) -> Unit
    ) : this(
        binding = ItemTokenHiddenBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onItemClicked = onItemClicked,
        onEditClicked = onEditClicked,
        onDeleteClicked = onDeleteClicked
    )

    companion object {
        private const val IMAGE_SIZE = 56
    }

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    private val tokenImageView = binding.tokenImageView
    private val wrappedImageView = binding.wrappedImageView
    private val nameTextView = binding.nameTextView
    private val addressTextView = binding.addressTextView
    private val valueTextView = binding.valueTextView
    private val totalTextView = binding.totalTextView
    private val colorView = binding.colorView
    private val hideImageView = binding.hideImageView
    private val editImageView = binding.editImageView
    private val contentView = binding.contentView

    fun onBind(item: TokenItem.Hidden, isZerosHidden: Boolean) {
        if (item.state is VisibilityState.Hidden) {
            itemView.isVisible = false
            return
        }

        itemView.isVisible = true
        val data = item.token
        if (!data.logoUrl.isNullOrEmpty()) {
            loadImage(tokenImageView, data.logoUrl)
        }
        wrappedImageView.isVisible = data.isWrapped
        nameTextView.text = data.tokenSymbol
        addressTextView.text = data.getFormattedAddress()
        valueTextView.text = data.getFormattedPrice()
        totalTextView.text = data.getFormattedTotal()
        colorView.setBackgroundColor(ContextCompat.getColor(colorView.context, data.color))
        hideImageView.setImageResource(data.getVisibilityIcon(isZerosHidden))
        hideImageView.setOnClickListener { onDeleteClicked(data) }
        editImageView.setOnClickListener { onEditClicked(data) }
        contentView.setOnClickListener { onItemClicked(data) }
    }

    private fun loadImage(imageView: ImageView, url: String) {
        if (url.contains(".svg")) {
            requestBuilder
                .load(Uri.parse(url))
                .apply(RequestOptions().override(IMAGE_SIZE, IMAGE_SIZE))
                .centerCrop()
                .into(imageView)
        } else {
            Glide.with(imageView).load(url).into(imageView)
        }
    }
}