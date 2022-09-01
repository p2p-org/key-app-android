package org.p2p.wallet.moonpay.ui.new

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemPaymentMethodBinding
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.utils.viewbinding.getString

class PaymentMethodViewHolder(
    private val binding: ItemPaymentMethodBinding,
    private val onClickListener: (PaymentMethod) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        onClickListener: (PaymentMethod) -> Unit
    ) : this(
        ItemPaymentMethodBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onClickListener
    )

    fun bind(method: PaymentMethod) = with(binding) {
        checkBoxSelected.isSelected = method.isSelected
        textViewFeeAmount.text = getString(R.string.buy_fee_percent, method.feePercent)
        textViewPaymentPeriod.text = getString(method.paymentPeriodResId)
        textViewPayingMethod.text = getString(method.methodResId)
        imageViewMethod.setImageResource(method.iconResId)
    }
}
