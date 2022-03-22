package org.p2p.wallet.receive.widget

import android.content.Context
import android.graphics.Bitmap
import android.text.Spannable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import org.p2p.wallet.databinding.WidgetReceiveCardViewBinding

class ReceiveCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val binding = WidgetReceiveCardViewBinding.inflate(LayoutInflater.from(context), this)

    fun setOnNetworkClickListener(block: () -> Unit) {
        binding.networkView.setOnClickListener { block() }
    }

    fun setOnFaqClickListener(block: () -> Unit) {
        binding.faqTextView.setOnClickListener { block() }
    }

    fun setSelectNetworkVisibility(isVisible: Boolean) {
        binding.networkView.isVisible = isVisible
    }

    fun setFaqVisibility(isVisible: Boolean) {
        binding.faqTextView.isVisible = isVisible
    }

    fun setQrName(name: String) {
        binding.qrView.setName(name)
    }

    fun getQrName() = binding.qrView.getName()

    fun setQrValue(value: Spannable) {
        binding.qrView.setValue(value)
    }

    fun setQrWatermark(@DrawableRes iconResId: Int) {
        binding.qrView.setWatermarkIcon(iconResId)
    }

    fun setQrWatermark(iconUrl: String?) {
        binding.qrView.setWatermarkIcon(iconUrl)
    }

    fun hideWatermark() = binding.qrView.hideWatermark()

    fun setNetworkName(newName: String) {
        binding.networkTextView.text = newName
    }

    fun setQrBitmap(bitmap: Bitmap) {
        binding.qrView.setImage(bitmap)
    }

    fun showQrLoading(isLoading: Boolean) {
        binding.qrView.showLoading(isLoading)
    }

    fun setOnSaveQrClickListener(block: (name: String, qrImage: Bitmap) -> Unit) {
        binding.qrView.onSaveClickListener = { name, qrImage -> block(name, qrImage) }
    }

    fun setOnShareQrClickListener(block: (qrValue: String, qrImage: Bitmap) -> Unit) {
        binding.qrView.onShareClickListener = { name, qrImage -> block(name, qrImage) }
    }

    fun setOnCopyQrClickListener(block: () -> Unit) {
        binding.qrView.onCopyClickListener = { block() }
    }
}
