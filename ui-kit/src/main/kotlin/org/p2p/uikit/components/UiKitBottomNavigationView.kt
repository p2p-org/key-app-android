package org.p2p.uikit.components

import android.content.Context
import android.util.AttributeSet
import android.view.Menu
import androidx.coordinatorlayout.widget.CoordinatorLayout
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetBottomNavigationViewBinding
import org.p2p.uikit.utils.inflateViewBinding

class UiKitBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr) {

    val binding = inflateViewBinding<WidgetBottomNavigationViewBinding>()

    fun getSelectedItemId(): Int = binding.bottomNavigationView.selectedItemId

    fun setSelectedItemId(itemId: Int) {
        binding.bottomNavigationView.selectedItemId = itemId
    }

    val menu: Menu
        get() = binding.bottomNavigationView.menu

    fun setOnItemSelectedListener(block: (ScreenTab) -> Boolean) {
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            ScreenTab.fromTabId(menuItem.itemId)?.let { block.invoke(it) } ?: false
        }
    }

    fun inflateMenu(menuResId: Int) {
        binding.bottomNavigationView.inflateMenu(menuResId)
    }

    fun setChecked(menuItemId: Int) {
        binding.bottomNavigationView.menu.findItem(menuItemId).isChecked = true
    }
}

enum class ScreenTab(val itemId: Int) {
    HOME_SCREEN(R.id.homeItem),
    HISTORY_SCREEN(R.id.historyItem),
    FEEDBACK_SCREEN(R.id.feedbackItem),
    SETTINGS_SCREEN(R.id.settingsItem);

    companion object {
        fun fromTabId(tabId: Int): ScreenTab? {
            return values()
                .firstOrNull { it.itemId == tabId }
        }
    }
}
