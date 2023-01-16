package org.p2p.wallet.common.ui.widget.actionbuttons

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.wallet.databinding.LayoutActionButtonsBinding
import org.p2p.wallet.sell.analytics.SellAnalytics

class ActionButtonsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent {

    var onButtonClicked: ((ActionButton) -> Unit)? = null

    private val binding: LayoutActionButtonsBinding = inflateViewBinding()

    private val sellAnalytics: SellAnalytics by inject()

    private val buttonsAdapter = ActionButtonsAdapter(onButtonClicked = {
        sellAnalytics.logCashOutClicked(SellAnalytics.AnalyticsCashOutSource.MAIN)
        onButtonClicked?.invoke(it)
    })

    init {
        binding.recyclerViewActionButtons.attachAdapter(buttonsAdapter)
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
