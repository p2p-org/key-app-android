package org.p2p.uikit.organisms

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import com.google.android.material.appbar.MaterialToolbar
import org.p2p.uikit.R

class UiKitToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.toolbarStyle
) : MaterialToolbar(context, attrs, defStyleAttr) {

    private var searchView: SearchView? = null

    init {
        minimumHeight = resources.getDimension(R.dimen.toolbar_height).toInt()
    }

    fun setSearchMenu(queryTextListener: SearchView.OnQueryTextListener, isMenuVisible: Boolean = true) {
        inflateMenu(R.menu.menu_ui_kit_toolbar_search)

        val search = menu.findItem(R.id.menu_search_view)
        searchView = search.actionView as SearchView
        searchView?.apply {
            isVisible = isMenuVisible
            setOnQueryTextListener(queryTextListener)
            if (isMenuVisible) {
                onActionViewExpanded()
            }
        }
    }

    fun toggleSearchView() {
        searchView?.apply {
            if (isShown) {
                isVisible = false
                onActionViewCollapsed()
            } else {
                isVisible = true
                onActionViewExpanded()
            }
        }
    }

    fun setSearchMenuItemVisibility(isVisible: Boolean) {
        menu?.findItem(R.id.menu_search_item)?.isVisible = isVisible
    }
}
