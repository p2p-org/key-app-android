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
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TransactionImage)

        val statusIconPadding = typedArray.getDimensionPixelSize(
            R.styleable.TransactionImage_statusIconPadding,
            0
        )

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
