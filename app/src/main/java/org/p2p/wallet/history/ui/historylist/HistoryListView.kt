package org.p2p.wallet.history.ui.historylist

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.koin.core.component.KoinComponent
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.wallet.databinding.LayoutHistoryListBinding

class HistoryListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent {

    private val binding = inflateViewBinding<LayoutHistoryListBinding>()
}
