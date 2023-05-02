package org.p2p.wallet.seed

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.uikit.organisms.seedphrase.adapter.SeedPhraseAdapter
import org.p2p.uikit.organisms.seedphrase.adapter.SeedPhraseParser
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetSeedPhraseViewBinding

class UiKitSeedPhraseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var onSeedPhraseChanged: ((List<SeedPhraseWord>) -> Unit)? = null
    var onShowKeyboardListener: ((Int) -> Unit)? = null

    private val binding = inflateViewBinding<WidgetSeedPhraseViewBinding>()

    private val seedPhraseAdapter: SeedPhraseAdapter by lazy(LazyThreadSafetyMode.NONE) {
        SeedPhraseAdapter(
            onSeedPhraseChanged = { keys ->
                setPasteButtonBackgroundColor(
                    isLime = keys.isEmpty() || keys.firstOrNull() == SeedPhraseWord.EMPTY_WORD
                )
                onSeedPhraseChanged?.invoke(keys)
            },
            onShowKeyboardListener = { onShowKeyboardListener?.invoke(it) }
        )
    }

    private val seedPhraseParser = SeedPhraseParser()

    init {
        with(binding) {
            keysRecyclerView.layoutManager = FlexboxLayoutManager(context).also {
                it.flexDirection = FlexDirection.ROW
                it.justifyContent = JustifyContent.FLEX_START
            }
            keysRecyclerView.attachAdapter(seedPhraseAdapter)

            textViewClear.setOnClickListener { seedPhraseAdapter.clear() }
            textViewPaste.setOnClickListener { addSeedPhraseFromClipboard() }

            setOnClickListener { onShowKeyboardListener?.invoke(seedPhraseAdapter.itemCount - 1) }
        }
    }

    private fun addSeedPhraseFromClipboard() {
        val clipboardValue = getClipboardText()
        val keysFromClipboard = seedPhraseParser.parse(
            if (clipboardValue.split(" ").size == 1) "$clipboardValue " else clipboardValue
        )
        if (keysFromClipboard.isNotEmpty()) seedPhraseAdapter.addAllWords(keysFromClipboard)
    }

    private fun setPasteButtonBackgroundColor(isLime: Boolean) {
        val backgroundRes = if (isLime) R.drawable.bg_lime_rounded_small else R.drawable.bg_rounded_small
        binding.textViewPaste.setBackgroundResource(backgroundRes)
    }

    fun updateSeedPhrase(secretKeys: List<SeedPhraseWord>, isEditable: Boolean = true) {
        seedPhraseAdapter.replaceWords(secretKeys, isEditable)
    }

    fun addSeedPhraseWord(seedPhraseWord: SeedPhraseWord) {
        seedPhraseAdapter.addWord(seedPhraseWord)
    }

    fun showFocusOnLastWord() {
        seedPhraseAdapter.showFocusOnLastItem()
    }

    fun showSeedPhraseValid(isValid: Boolean) {
        val textRes = if (isValid) R.string.seed_phrase_view_valid else R.string.seed_phrase_view_invalid
        binding.textViewValidationTitle.setText(textRes)
    }

    fun showClearButton(isVisible: Boolean) {
        binding.textViewClear.isVisible = isVisible
    }

    fun showPasteButton(isVisible: Boolean) {
        binding.textViewPaste.isVisible = isVisible
    }

    fun showBlurButton(isVisible: Boolean) {
        binding.textViewBlur.isVisible = isVisible
    }

    fun setOnBlurStateChangedListener(block: (Boolean) -> Unit) {
        binding.textViewBlur.setOnClickListener {
            toggleBlurState()
            block.invoke(it.isSelected)
        }
    }

    // Getting clipboard here since it's impossible to move `ContextExtensions` to ui-kit at the moment
    // TODO: Use Extensions for getting data from clipboard
    private fun getClipboardText(): String {
        val clipboard = context.getSystemService<ClipboardManager>()
        val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
        return text?.trim().orEmpty()
    }

    private fun toggleBlurState() = with(binding) {
        val isSelected = textViewBlur.isSelected
        if (isSelected) {
            textViewBlur.setText(R.string.common_show)
            textViewBlur.isSelected = false
        } else {
            textViewBlur.setText(R.string.common_hide)
            textViewBlur.isSelected = true
        }
    }
}
