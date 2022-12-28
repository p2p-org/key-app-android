package org.p2p.wallet.common.ui.widget.actionbuttons

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.wallet.databinding.LayoutActionButtonsBinding
import org.p2p.wallet.utils.unsafeLazy

class ActionButtonsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var onButtonClicked: ((ActionButton) -> Unit)? = null

    private val binding: LayoutActionButtonsBinding = inflateViewBinding()

    private val buttonsAdapter: ActionButtonsAdapter by unsafeLazy {
        ActionButtonsAdapter { onButtonClicked?.invoke(it) }
    }

    init {
        val flexboxLayoutManager = FlexboxLayoutManager(context, FlexDirection.ROW)
        flexboxLayoutManager.justifyContent = JustifyContent.CENTER
        binding.recyclerViewActionButtons.layoutManager = flexboxLayoutManager
        binding.recyclerViewActionButtons.adapter = buttonsAdapter
    }

    fun showActionButtons(buttons: List<ActionButton>) {
        buttonsAdapter.setItems(buttons)
    }
}

enum class ActionButton {
    BUY_BUTTON,
    RECEIVE_BUTTON,
    SEND_BUTTON,
    SWAP_BUTTON,
    SELL_BUTTON
}
