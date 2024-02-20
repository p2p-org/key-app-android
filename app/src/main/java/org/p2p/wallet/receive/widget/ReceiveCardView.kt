package org.p2p.wallet.receive.widget

import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import android.content.Context
import android.graphics.Bitmap
import android.text.Spannable
import android.util.AttributeSet
import org.p2p.wallet.databinding.WidgetReceiveCardViewBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class ReceiveCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val binding = inflateViewBinding<WidgetReceiveCardViewBinding>()

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

    fun setTokenSymbol(symbol: String) {
        binding.qrView.setTokenSymbol(symbol)
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

    fun setOnRequestPermissions(isGranted: () -> Boolean) {
        binding.qrView.onRequestPermissions = { isGranted() }
    }

    fun setOnSaveQrClickListener(block: (name: String, qrImage: Bitmap) -> Unit) {
        binding.qrView.onSaveClickListener = { name, qrImage -> block(name, qrImage) }
    }

    fun requestSave() = binding.qrView.requestAction(QrView.QrCodeAction.SAVE)

    fun setOnShareQrClickListener(block: (qrValue: String, qrImage: Bitmap, shareText: String) -> Unit) {
        binding.qrView.onShareClickListener = { name, qrImage, shareText -> block(name, qrImage, shareText) }
    }

    fun requestShare() = binding.qrView.requestAction(QrView.QrCodeAction.SHARE)

    fun setOnCopyQrClickListener(block: () -> Unit) {
        binding.qrView.onCopyClickListener = { block() }
    }

    fun getQrCodeLastAction(): QrView.QrCodeAction? = binding.qrView.qrCodeLastAction

    fun setChevronInvisible(isInvisible: Boolean) {
        binding.arrowImageView.isInvisible = isInvisible
    }
}
