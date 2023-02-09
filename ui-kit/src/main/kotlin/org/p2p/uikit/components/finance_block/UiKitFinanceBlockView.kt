package org.p2p.uikit.components.finance_block

import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.databinding.UiKitFinanceBlockBinding
import org.p2p.uikit.utils.inflateViewBinding

class UiKitFinanceBlockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = inflateViewBinding<UiKitFinanceBlockBinding>()


    private fun bind(){

    }
}
