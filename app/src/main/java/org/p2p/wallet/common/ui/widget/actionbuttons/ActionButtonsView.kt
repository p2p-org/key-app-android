package org.p2p.wallet.common.ui.widget.actionbuttons

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.context
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.wallet.R
import org.p2p.wallet.databinding.LayoutActionButtonsBinding
import org.p2p.wallet.sell.analytics.SellAnalytics
import org.p2p.wallet.utils.HomeScreenLayoutManager

class ActionButtonsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent {

    var onButtonClicked: ((ActionButton) -> Unit)? = null

    private val binding: LayoutActionButtonsBinding = inflateViewBinding()

    private val layoutManager: HomeScreenLayoutManager by lazy {
        HomeScreenLayoutManager(context = binding.context)
            .apply { orientation = RecyclerView.HORIZONTAL }
    }

    private val sellAnalytics: SellAnalytics by inject()

    private val buttonsAdapter = ActionButtonsAdapter(onButtonClicked = {
        if (it == ActionButton.SELL_BUTTON) {
            sellAnalytics.logCashOutClicked(SellAnalytics.AnalyticsCashOutSource.MAIN)
        }
        onButtonClicked?.invoke(it)
    })

    init {
        with(binding) {
            recyclerViewActionButtons.layoutManager = layoutManager
            recyclerViewActionButtons.attachAdapter(buttonsAdapter)
        }
    }

    fun showActionButtons(buttons: List<ActionButton>) {
        buttonsAdapter.setItems(buttons)
    }
}

/**
 * Order of elements is used to display these buttons in the same order
 */
enum class ActionButton(
    @IdRes val viewId: Int,
    @StringRes val textRes: Int,
    @DrawableRes val iconRes: Int
) {
    RECEIVE_BUTTON(
        viewId = R.id.buttonReceive,
        textRes = R.string.home_receive,
        iconRes = R.drawable.ic_receive
    ),
    SELL_BUTTON(
        viewId = R.id.actionButtonSell,
        textRes = R.string.home_sell,
        iconRes = R.drawable.ic_dollar_sign
    ),
    SWAP_BUTTON(
        viewId = R.id.actionButtonSwap,
        textRes = R.string.home_swap,
        iconRes = R.drawable.ic_swap
    ),
    BUY_BUTTON(
        viewId = R.id.actionButtonBuy,
        textRes = R.string.home_buy,
        iconRes = R.drawable.ic_plus
    ),
    SEND_BUTTON(
        viewId = R.id.actionButtonSend,
        textRes = R.string.home_send,
        iconRes = R.drawable.ic_arrow_up
    ),
    TOP_UP_BUTTON(
        viewId = R.id.actionButtonTopUp,
        textRes = R.string.home_top_up,
        iconRes = R.drawable.ic_plus
    ),
}
