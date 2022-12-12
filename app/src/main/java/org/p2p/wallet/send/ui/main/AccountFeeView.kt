package org.p2p.wallet.send.ui.main

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import android.content.Context
import android.util.AttributeSet
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetAccountFeeViewBinding
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.core.utils.asApproximateUsd
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import java.math.BigDecimal

class AccountFeeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), KoinComponent {

    private val binding: WidgetAccountFeeViewBinding = inflateViewBinding()

    private val glideManager: GlideManager by inject()

    fun showFee(fee: SendSolanaFee) {
        with(binding) {
            fillUsdFee(fee.feeUsd)

            accountImageView.background = null
            accountFeeValueTextView.text = fee.accountCreationFormattedFee
            accountFeeValueTextView.setTextColor(getColor(R.color.textIconPrimary))
            glideManager.load(accountImageView, fee.feePayerToken.iconUrl)
        }
    }

    fun showInsufficientView(feeUsd: BigDecimal?) {
        with(binding) {
            fillUsdFee(feeUsd)

            accountImageView.setBackgroundResource(R.drawable.bg_error_rounded_12)
            accountImageView.setImageResource(R.drawable.ic_error)
            accountFeeValueTextView.text = context.getString(R.string.send_not_enough_funds)
            accountFeeValueTextView.setTextColor(getColor(R.color.systemErrorMain))
        }
    }

    fun setLoading(isLoading: Boolean) {
        with(binding) {
            shimmerView.isVisible = isLoading

            accountImageView.isInvisible = isLoading
            accountFeeTextView.isInvisible = isLoading
            accountFeeValueTextView.isInvisible = isLoading
            arrowImageView.isInvisible = isLoading
        }
    }

    private fun fillUsdFee(feeUsd: BigDecimal?) {
        val usdFeeValue = feeUsd?.asApproximateUsd(withBraces = false)
        val result = if (usdFeeValue.isNullOrEmpty()) context.getString(R.string.common_not_available) else usdFeeValue
        binding.accountFeeTextView.text = context.getString(R.string.send_account_creation_fee_format, result)
    }
}
