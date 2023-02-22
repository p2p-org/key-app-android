package org.p2p.uikit.organisms

import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import android.content.Context
import android.util.AttributeSet
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
) : MaterialToolbar(context, attrs, defStyleAttr), SearchView.OnQueryTextListener {

    var onQueryUpdated: ((String) -> Unit)? = null

    private var searchView: SearchView? = null

    init {
        minimumHeight = resources.getDimension(R.dimen.toolbar_height).toInt()
    }

    fun setSearchMenu(
        @MenuRes menuRes: Int = R.menu.menu_ui_kit_toolbar_search,
        @StringRes searchHintRes: Int = R.string.common_search,
        showKeyboard: Boolean = true
    ) {
        inflateMenu(menuRes)


        val search = menu.findItem(R.id.menuItemSearch)
        searchView = search.actionView as SearchView
        searchView!!.apply {
            queryHint = getString(searchHintRes)
            onActionViewExpanded()
            if (showKeyboard) {
                showSoftKeyboard()
            }
            setOnQueryTextListener(this@UiKitToolbar)
        }
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        onQueryUpdated?.invoke(newText.orEmpty())
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean = false

    fun setQuery(query: String, submit: Boolean) {
        searchView?.setQuery(query, submit)
    }

    fun setOnDoneListener(onDoneClicked: () -> Unit) {
        val searchEditText = searchView?.findViewById<EditText>(androidx.appcompat.R.id.search_src_text) ?: return
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onDoneClicked()
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }
    }
}
