package org.p2p.uikit.organisms.seedphrase

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.view.children
import androidx.core.view.isVisible
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetSeedPhraseViewBinding
import org.p2p.uikit.organisms.seedphrase.adapter.SeedPhraseAdapter
import org.p2p.uikit.organisms.seedphrase.adapter.SeedPhraseParser
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.showSoftKeyboard

class UiKitSeedPhraseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var onSeedPhraseChanged: ((List<SeedPhraseWord>) -> Unit)? = null

    private val binding = inflateViewBinding<WidgetSeedPhraseViewBinding>()

    private val phraseAdapter: SeedPhraseAdapter by lazy {
        SeedPhraseAdapter { keys ->
            setPasteButtonEnabled(isEnabled = keys.isEmpty() || keys.firstOrNull() == SeedPhraseWord.EMPTY_WORD)
            onSeedPhraseChanged?.invoke(keys)
        }
    }

    private val seedPhraseParser = SeedPhraseParser()

    init {
        setBackgroundResource(R.drawable.bg_smoke_rounded)

        binding.keysRecyclerView.layoutManager = FlexboxLayoutManager(context).also {
            it.flexDirection = FlexDirection.ROW
            it.justifyContent = JustifyContent.FLEX_START
        }
        binding.keysRecyclerView.attachAdapter(phraseAdapter)

        binding.textViewClear.setOnClickListener { phraseAdapter.clear() }
        binding.textViewPaste.setOnClickListener { addSeedPhraseFromClipboard() }
    }

    private fun addSeedPhraseFromClipboard() {
        val clipboardValue =  getClipboardText()
        val keysFromClipboard = seedPhraseParser.parse(
            if (clipboardValue.split(" ").size == 1) "$clipboardValue " else clipboardValue
        )
        if (keysFromClipboard.isNotEmpty()) phraseAdapter.addAllSecretKeys(keysFromClipboard)
    }

    private fun setPasteButtonEnabled(isEnabled: Boolean) {
        binding.textViewPaste.isEnabled = isEnabled
    }

    fun updateSecretKeys(secretKeys: List<SeedPhraseWord>) {
        phraseAdapter.updateSecretKeys(secretKeys)
    }

    fun addSecretKey(seedPhraseWord: SeedPhraseWord) {
        phraseAdapter.addSecretKey(seedPhraseWord)
    }

    fun showFocusOnLastKey(lastSecretItemIndex: Int) {
        val viewGroup = binding.keysRecyclerView.children
            .toList()
            .getOrNull(lastSecretItemIndex) as? LinearLayout
            ?: return

        val secretKeyEditText = viewGroup.children.firstOrNull { it.id == R.id.editTextWord }
        secretKeyEditText?.requestFocus()
        secretKeyEditText?.showSoftKeyboard()
    }

    fun showSeedPhraseValid(isValid: Boolean) {
        val textRes = if (isValid) R.string.seed_phrase_view_valid else R.string.seed_phrase_view_invalid
        binding.textViewValidationTitle.setText(textRes)
    }

    fun showClearButton(isVisible: Boolean) {
        binding.textViewClear.isVisible = isVisible
    }

    fun setOnContainerClickListener(callback: () -> Unit) {
        binding.keysRecyclerView.setOnClickListener { callback() }
    }

    // Getting clipboard here since it's impossible to move `ContextExtensions` to ui-kit at the moment
    // TODO: Use Extensions for getting data from clipboard
    private fun getClipboardText(): String {
        val clipboard = context.getSystemService<ClipboardManager>()
        val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
        return text?.trim().orEmpty()
    }
}
