package org.p2p.wallet.svl.ui.send

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import android.content.Context
import android.util.AttributeSet
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetSendViaLinkBinding
import org.p2p.wallet.svl.model.SvlWidgetState
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class SendViaLinkWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val viewBinding = inflateViewBinding<WidgetSendViaLinkBinding>()

    init {
        setBackgroundResource(R.drawable.bg_rounded_solid_snow_16)
        foreground = context.getDrawable(R.drawable.ripple_button_large)
    }

    fun updateState(newState: SvlWidgetState) {
        when (newState) {
            SvlWidgetState.ENABLED -> {
                viewBinding.viewDisabled.isVisible = false
                viewBinding.textViewSubtitle.setText(R.string.send_dont_need_the_address)
            }
            SvlWidgetState.DISABLED -> {
                viewBinding.viewDisabled.isVisible = true
                viewBinding.textViewSubtitle.setText(R.string.send_via_link_limit_exceeded)
            }
        }
    }

    fun setOnClickListener(listener: () -> Unit) {
        viewBinding.root.setOnClickListener { listener() }
    }
}
