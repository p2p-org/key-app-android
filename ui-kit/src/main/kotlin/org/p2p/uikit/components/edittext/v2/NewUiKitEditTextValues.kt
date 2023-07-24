package org.p2p.uikit.components.edittext.v2

import androidx.core.view.isVisible
import android.graphics.drawable.Drawable
import android.widget.EditText
import org.p2p.uikit.databinding.WidgetUiKitEditTextNewBinding

/**
 * All the values that can be fetched from this view
 */
class NewUiKitEditTextValues(
    private val binding: WidgetUiKitEditTextNewBinding,
) {
    private val input: EditText = binding.editTextField

    val inputText: String get() = input.text?.toString().orEmpty()
    val hint: CharSequence get() = input.hint
    val inputTextLength: Int get() = inputText.length
    val endDrawable: Drawable? get() = binding.imageViewIconEnd.drawable
    val isEndDrawableVisible: Boolean get() = binding.imageViewIconEnd.isVisible
}
