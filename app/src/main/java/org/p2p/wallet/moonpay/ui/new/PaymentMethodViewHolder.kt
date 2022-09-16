package org.p2p.wallet.moonpay.ui.new

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemPaymentMethodBinding
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class PaymentMethodViewHolder(
    parent: ViewGroup,
    private val onClickListener: (PaymentMethod) -> Unit,
    private val binding: ItemPaymentMethodBinding = parent.inflateViewBinding(attachToRoot = false)
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(method: PaymentMethod) = with(binding) {
        checkBox.isChecked = method.isSelected
        textViewFeeAmount.text = getString(R.string.buy_fee_percent, method.feePercent)
        textViewPaymentPeriod.text = getString(method.paymentPeriodResId)
        textViewPayingMethod.text = getString(method.methodResId)
        imageViewMethod.setImageResource(method.iconResId)
        checkBox.setOnClickListener { onClickListener(method) }
        root.setOnClickListener { onClickListener(method) }
    }
}
