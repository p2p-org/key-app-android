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

    var selectedItemId: Int = binding.bottomNavigationView.selectedItemId

    val menu: Menu
        get() = binding.bottomNavigationView.menu

    fun setOnItemSelectedListener(block: (ScreenTab) -> Boolean) {
        binding.bottomNavigationView.setOnItemSelectedListener {
            val foundTab = ScreenTab.fromTabId(it.itemId)
            if (foundTab != null) {
                block.invoke(foundTab)
            }
            return@setOnItemSelectedListener true
        }
    }

    fun setOnCenterActionButtonListener(block: () -> Unit) {
        binding.buttonCenterAction.setOnClickListener { block.invoke() }
    }

    fun inflateMenu(menuResId: Int) {
        binding.bottomNavigationView.inflateMenu(menuResId)
    }
}

enum class ScreenTab(val itemId: Int) {
    HOME_SCREEN(R.id.homeItem),
    HISTORY_SCREEN(R.id.historyItem),
    SETTINGS_SCREEN(R.id.settingsItem);

    companion object {
        fun fromTabId(tabId: Int): ScreenTab? {
            return values()
                .firstOrNull { it.itemId == tabId }
        }
    }
}
