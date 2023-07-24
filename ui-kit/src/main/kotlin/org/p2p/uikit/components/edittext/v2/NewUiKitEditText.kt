package org.p2p.uikit.components.edittext.v2

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import org.p2p.uikit.R
import org.p2p.uikit.components.edittext.UiKitEditTextSavedState
import org.p2p.uikit.databinding.WidgetUiKitEditTextNewBinding
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.uikit.utils.inflateViewBinding

class NewUiKitEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ConstraintLayout(context, attrs) {
    private val binding: WidgetUiKitEditTextNewBinding by lazy(LazyThreadSafetyMode.NONE, ::inflateViewBinding)

    val values: NewUiKitEditTextValues = NewUiKitEditTextValues(binding)
    private val mutator: NewUiKitEditTextMutator = NewUiKitEditTextMutator(binding)

    init {
        val styleAttrsApplier = NewUiKitEditTextStyleAttrsApplier()
        context.obtainStyledAttributes(attrs, R.styleable.NewUiKitEditText, 0, 0)
            .use { styleAttrsApplier.applyToView(it, binding) }
    }

    fun mutate(): NewUiKitEditTextMutator = mutator

    fun focusAndShowKeyboard() {
        binding.editTextField.focusAndShowKeyboard()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return UiKitEditTextSavedState(superState, values.inputText)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is UiKitEditTextSavedState) {
            super.onRestoreInstanceState(state.superState)
            mutator.setText(state.text.orEmpty())
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}
