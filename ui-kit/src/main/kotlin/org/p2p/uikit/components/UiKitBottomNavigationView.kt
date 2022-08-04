package org.p2p.uikit.components

import android.content.Context
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import androidx.constraintlayout.widget.ConstraintLayout
import org.p2p.uikit.databinding.WidgetBottomNavigationViewBinding
import org.p2p.uikit.utils.inflateViewBinding

class UiKitBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    enum class ScreenTabs {
        HOME_SCREEN, HISTORY_SCREEN, SEND_SCREEN, FEEDBACK_SCREEN, SETTINGS_SCREEN
    }

    val binding = inflateViewBinding<WidgetBottomNavigationViewBinding>()

    var selectedItemId: Int = binding.bottoNavigationView.selectedItemId

    val menu: Menu
        get() = binding.bottoNavigationView.menu

    fun setOnItemSelectedListener(block: (MenuItem) -> Boolean) {
        binding.bottoNavigationView.setOnItemSelectedListener {
            return@setOnItemSelectedListener block(it)
        }
    }

    fun inflateMenu(resId: Int) {
        binding.bottoNavigationView.inflateMenu(resId)
    }
}
