package org.p2p.uikit.organisms.seedphrase

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import android.content.Context
import android.util.AttributeSet
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetSeedPhraseViewBinding
import org.p2p.uikit.utils.inflateViewBinding

class UiKitSeedPhraseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = inflateViewBinding<WidgetSeedPhraseViewBinding>()

    init {
        setBackgroundResource(R.drawable.bg_smoke_rounded)
        minimumHeight = resources.getDimension(R.dimen.ui_kit_seed_phrase_container_min_height).toInt()
    }

    fun showSeedPhraseValid(isValid: Boolean) {
        val textRes = if (isValid) R.string.seed_phrase_check else R.string.seed_phrase
        binding.textViewType.setText(textRes)
    }

    fun showClearButton(isVisible: Boolean) {
        binding.textViewClear.isVisible = isVisible
    }

    fun setPasteEnabled(isEnabled: Boolean) {
        binding.textViewPaste.isEnabled = isEnabled
    }

    fun setOnPasteClickListener(callback: () -> Unit) {
        binding.textViewPaste.setOnClickListener { callback() }
    }

    fun setOnClearClickListener(callback: () -> Unit) {
        binding.textViewClear.setOnClickListener { callback() }
    }

    fun setOnContainerClickListener(callback: () -> Unit) {
        binding.keysRecyclerView.setOnClickListener { callback() }
    }
}
