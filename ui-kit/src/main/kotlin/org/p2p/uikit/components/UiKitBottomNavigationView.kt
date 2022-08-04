package org.p2p.uikit.components

import android.content.Context
import android.util.AttributeSet
import android.view.Menu
import androidx.constraintlayout.widget.ConstraintLayout
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetBottomNavigationViewBinding
import org.p2p.uikit.utils.inflateViewBinding

class UiKitBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val binding = inflateViewBinding<WidgetBottomNavigationViewBinding>()

    init {
        binding.bottomNavigationView.inflateMenu(R.menu.menu_ui_kit_bottom_navigation)
    }

    var selectedItemId: ScreenTab = getScreenTabByMenuItemId(binding.bottomNavigationView.selectedItemId)

    val menu: Menu
        get() = binding.bottomNavigationView.menu

    fun setOnItemSelectedListener(block: (ScreenTab) -> Boolean) {
        binding.bottomNavigationView.setOnItemSelectedListener {
            val screenTabItem = getScreenTabByMenuItemId(it.itemId)
            block(screenTabItem)
            return@setOnItemSelectedListener true
        }
    }

    fun setOnCenterActionButtonListener(block: () -> Unit) {
        binding.buttonCenterAction.setOnClickListener { block.invoke() }
    }

    private fun getScreenTabByMenuItemId(menuItemId: Int): ScreenTab {
        return when (menuItemId) {
            R.id.homeItem -> ScreenTab.HOME_SCREEN
            R.id.historyItem -> ScreenTab.HISTORY_SCREEN
            R.id.sendItem -> ScreenTab.SEND_SCREEN
            R.id.feedbackItem -> ScreenTab.FEEDBACK_SCREEN
            R.id.settingsItem -> ScreenTab.SETTINGS_SCREEN
            else -> throw IllegalStateException("No screen tab found for menu item = $menuItemId")
        }
    }

    enum class ScreenTab {
        HOME_SCREEN, HISTORY_SCREEN, SEND_SCREEN, FEEDBACK_SCREEN, SETTINGS_SCREEN
    }
}
