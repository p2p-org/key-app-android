package org.p2p.wallet.receive.widget

import android.content.Context
import android.graphics.Bitmap
import android.text.Spannable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetQrViewBinding
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.createBitmap
import org.p2p.wallet.utils.toast

class QrView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = WidgetQrViewBinding.inflate(LayoutInflater.from(context), this)
    var onShareClickListener: ((String, Bitmap) -> Unit)? = null
    var onCopyClickListener: (() -> Unit)? = null
    var onSaveClickListener: ((String, Bitmap) -> Unit)? = null

    fun setValue(qrValue: Spannable) {
        with(binding) {
            valueTextView.text = qrValue
            valueTextView.isVisible = qrValue.isNotEmpty()
            valueTextView.setOnClickListener {
                context.copyToClipBoard(qrValue.toString())
                context.toast(R.string.main_receive_address_copied)
            }
            saveButton.setOnClickListener {
                showSnapshotAnimation(QrCodeAction.SAVE)
            }
            shareButton.setOnClickListener {
                showSnapshotAnimation(QrCodeAction.SHARE)
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
            nameTextView.text = qrName
            nameTextView.isVisible = qrName.isNotEmpty()
            nameTextView.setOnClickListener {
                context.copyToClipBoard(qrName)
                context.toast(R.string.receive_username_copied)
            }
        }
    }

    fun getName() = binding.nameTextView.text.toString()

    fun setImage(bitmap: Bitmap) {
        binding.qrImageView.setImageBitmap(bitmap)
    }

    fun hideWatermark() {
        binding.watermarkImageViewBackground.isVisible = false
    }

    fun setWatermarkIcon(@DrawableRes iconResId: Int) {
        binding.watermarkImageView.setImageResource(iconResId)
    }

    fun setWatermarkIcon(iconUrl: String?) {
        Glide.with(this).load(iconUrl).into(binding.watermarkImageView)
    }

    fun showLoading(isLoading: Boolean) {
        binding.qrImageView.isVisible = !isLoading
        binding.progressBar.isVisible = isLoading
    }

    private fun showSnapshotAnimation(action: QrCodeAction) {
        val animation = AlphaAnimation(1.0f, 0.0f)
        animation.duration = 200L
        animation.fillAfter = true
        animation.repeatCount = 1
        animation.interpolator = LinearInterpolator()
        animation.repeatMode = Animation.REVERSE
        var bitmap: Bitmap

        animation.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation?) {
                binding.actionContainer.isVisible = false
                binding.logoImageView.isVisible = true
                bitmap = binding.root.createBitmap()
            }

            override fun onAnimationEnd(animation: Animation?) {
                onSnapshotReady(binding.root.createBitmap(), action)
                binding.actionContainer.isVisible = true
                binding.logoImageView.isVisible = false
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        binding.root.startAnimation(animation)
    }

    private fun onSnapshotReady(bitmap: Bitmap, action: QrCodeAction) {
        val qrValue = binding.valueTextView.text.toString()
        when (action) {
            QrCodeAction.SHARE -> onShareClickListener?.invoke(qrValue, bitmap)
            QrCodeAction.SAVE -> onSaveClickListener?.invoke(qrValue, bitmap)
        }
    }

    enum class QrCodeAction {
        SHARE, SAVE
    }
}
