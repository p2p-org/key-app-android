package org.p2p.wallet.receive.widget

import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import android.content.Context
import android.graphics.Bitmap
import android.text.Spannable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import com.bumptech.glide.Glide
import org.p2p.uikit.utils.createBitmap
import org.p2p.uikit.utils.toast
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetQrViewBinding
import org.p2p.wallet.utils.copyToClipBoard

class QrView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = WidgetQrViewBinding.inflate(LayoutInflater.from(context), this)
    private var tokenSymbol: String? = null

    var onShareClickListener: ((String, Bitmap, String) -> Unit)? = null
    var onCopyClickListener: (() -> Unit)? = null
    var onSaveClickListener: ((String, Bitmap) -> Unit)? = null
    var onRequestPermissions: (() -> Boolean)? = null

    var qrCodeLastAction: QrCodeAction? = null

    fun setValue(qrValue: Spannable) {
        with(binding) {
            valueTextView.text = qrValue
            valueTextView.isVisible = qrValue.isNotEmpty()
            valueTextView.setOnClickListener {
                context.copyToClipBoard(qrValue.toString())
                context.toast(R.string.main_receive_address_copied)
            }
            saveButton.setOnClickListener {
                qrCodeLastAction = QrCodeAction.SAVE
                if (onRequestPermissions?.invoke() == true) {
                    showSnapshotAnimation(QrCodeAction.SAVE)
                }
            }
            shareButton.setOnClickListener {
                qrCodeLastAction = QrCodeAction.SHARE
                if (onRequestPermissions?.invoke() == true) {
                    showSnapshotAnimation(QrCodeAction.SHARE)
                }
            }
            copyButton.setOnClickListener {
                context.copyToClipBoard(qrValue.toString())
                context.toast(R.string.common_copied)
                onCopyClickListener?.invoke()
            }
        }
    }

    fun setName(qrName: String) {
        with(binding) {
            textViewName.text = qrName
            textViewName.isVisible = qrName.isNotEmpty()
            textViewName.setOnClickListener {
                context.copyToClipBoard(qrName)
                context.toast(R.string.receive_username_copied)
            }
        }
    }

    fun getName(): String = binding.textViewName.text.toString()

    fun setImage(bitmap: Bitmap) {
        binding.imageViewQr.setImageBitmap(bitmap)
    }

    fun hideWatermark() {
        binding.containerWatermark.isVisible = false
    }

    fun setWatermarkIcon(@DrawableRes iconResId: Int) {
        binding.imageViewWatermark.setImageResource(iconResId)
    }

    fun setWatermarkIcon(iconUrl: String?) {
        Glide.with(this).load(iconUrl).into(binding.imageViewWatermark)
    }

    fun setTokenSymbol(symbol: String) {
        tokenSymbol = symbol
    }

    fun showLoading(isLoading: Boolean) {
        binding.imageViewQr.isVisible = !isLoading
        binding.progressBar.isVisible = isLoading
    }

    fun requestAction(action: QrCodeAction) = showSnapshotAnimation(action)

    private fun showSnapshotAnimation(action: QrCodeAction) {
        val animation = AlphaAnimation(1.0f, 0.0f)
        animation.duration = 200L
        animation.fillAfter = true
        animation.repeatCount = 1
        animation.interpolator = LinearInterpolator()
        animation.repeatMode = Animation.REVERSE

        animation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation?) {
                binding.actionContainer.isVisible = false
                binding.logoImageView.isVisible = true
            }

            override fun onAnimationEnd(animation: Animation?) {
                onSnapshotReady(binding.root.createBitmap(), action)
                binding.actionContainer.isVisible = true
                binding.logoImageView.isVisible = false
            }

            override fun onAnimationRepeat(animation: Animation?) = Unit
        })
        binding.root.startAnimation(animation)
    }

    private fun onSnapshotReady(bitmap: Bitmap, action: QrCodeAction) {
        val qrValue = binding.valueTextView.text.toString()
        val name = "$qrValue.${tokenSymbol.orEmpty()}"
        when (action) {
            QrCodeAction.SHARE -> onShareClickListener?.invoke(name, bitmap, qrValue)
            QrCodeAction.SAVE -> onSaveClickListener?.invoke(name, bitmap)
        }
    }

    enum class QrCodeAction {
        SHARE, SAVE
    }
}
