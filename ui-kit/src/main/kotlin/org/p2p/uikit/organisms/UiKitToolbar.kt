package org.p2p.uikit.organisms

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.google.android.material.appbar.MaterialToolbar
import org.p2p.uikit.R
import org.p2p.uikit.utils.getString
import org.p2p.uikit.utils.showSoftKeyboard

class UiKitToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.toolbarStyle
) : MaterialToolbar(context, attrs, defStyleAttr) {

    var searchView: SearchView? = null

    init {
        minimumHeight = resources.getDimension(R.dimen.toolbar_height).toInt()
    }

    fun setSearchMenu(
        queryTextListener: SearchView.OnQueryTextListener,
        @MenuRes menuRes: Int = R.menu.menu_ui_kit_toolbar_search,
        @StringRes searchHintRes: Int = R.string.common_search,
        isMenuVisible: Boolean = true,
        lastQuery: String? = null
    ) {
        inflateMenu(menuRes)

        val search = menu.findItem(R.id.menuItemSearch)
        searchView = search.actionView as SearchView
        searchView?.apply {
            // need to set old query and call onActionViewExpanded() because setText="" called in it!
            setQuery(lastQuery, false)
            queryHint = getString(searchHintRes)
            isVisible = isMenuVisible
            if (isMenuVisible) {
                onActionViewExpanded()
                showSoftKeyboard()
            }
            setOnQueryTextListener(queryTextListener)
        }
    }

    fun setOnDoneListener(callback: () -> Unit) {
        val searchEditText = searchView?.findViewById<EditText>(androidx.appcompat.R.id.search_src_text) ?: return
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                callback()
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
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
                showSoftKeyboard()
            }
        }
    }

    fun setSearchMenuItemVisibility(isVisible: Boolean) {
        menu?.findItem(R.id.menuItemSearch)?.isVisible = isVisible
    }
}
