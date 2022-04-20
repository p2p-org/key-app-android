package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetTransactionImageBinding
import org.p2p.wallet.transaction.model.TransactionStatus
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withImageOrGone

class TransactionImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = inflateViewBinding<WidgetTransactionImageBinding>()

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TransactionImageView)

        val statusIconSize = typedArray.getDimensionPixelSize(
            R.styleable.TransactionImageView_statusIconSize,
            resources.getDimensionPixelSize(R.dimen.history_transaction_image_icon_size)
        )

        with(binding.transactionStatus) {
            layoutParams = layoutParams.also {
                it.height = statusIconSize
                it.width = statusIconSize
            }
        }

        typedArray.recycle()
    }

    fun setTransactionIcon(@DrawableRes iconRes: Int) {
        binding.transactionTokenImageView.setImageResource(iconRes)
    }

    fun setStatus(status: TransactionStatus?) {
        binding.transactionStatus.withImageOrGone(getStatusIcon(status))
    }

    private fun getStatusIcon(status: TransactionStatus?): Int? = when (status) {
        TransactionStatus.PENDING -> R.drawable.ic_state_pending
        TransactionStatus.ERROR -> R.drawable.ic_state_error
        else -> null
    }
}
