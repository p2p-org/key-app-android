package org.p2p.uikit.organisms

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import org.p2p.uikit.R
import org.p2p.wallet.common.extensions.getDrawableCompat

class UiKitBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    enum class ScreenTabs {
        HOME_SCREEN, HISTORY_SCREEN, SEND_SCREEN, FEEDBACK_SCREEN, SETTINGS_SCREEN
    }

    var onNavigationItemClicked: (ScreenTabs) -> Unit = {}

    init {
        inflateMenu(R.menu.menu_ui_kit_bottom_navigation)

        minimumHeight = resources.getDimension(R.dimen.bottom_navigation_height).toInt()

        labelVisibilityMode = LABEL_VISIBILITY_LABELED
        itemBackground = context.getDrawableCompat(R.drawable.shape_bottom_navigation_selected)

        NavigationView.OnNavigationItemSelectedListener { item ->
            val screenTab = when (item.itemId) {
                R.id.homeItem -> ScreenTabs.HOME_SCREEN
                R.id.historyItem -> ScreenTabs.HISTORY_SCREEN
                R.id.sendItem -> ScreenTabs.SEND_SCREEN
                R.id.feedbackItem -> ScreenTabs.FEEDBACK_SCREEN
                R.id.settingsItem -> ScreenTabs.SETTINGS_SCREEN
                else -> return@OnNavigationItemSelectedListener false
            }

            onNavigationItemClicked.invoke(screenTab)
            true
        }
    }
}
