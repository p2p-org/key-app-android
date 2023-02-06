package org.p2p.uikit.organisms.seedphrase.adapter

import android.graphics.BlurMaskFilter
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.p2p.uikit.R
import org.p2p.uikit.databinding.ItemSecretKeyBinding
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.uikit.organisms.seedphrase.adapter.SeedPhraseConstants.SEED_PHRASE_SIZE_LONG
import org.p2p.uikit.utils.SpanUtils
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.uikit.utils.showSoftKeyboard

class SeedPhraseWordViewHolder(
    parent: ViewGroup,
    private val onKeyRemovedListener: (Int) -> Unit,
    private val onUpdateKeyListener: (SeedPhraseWord) -> Unit,
    private val onInsertedListener: (List<SeedPhraseWord>) -> Unit,
    private val onShowKeyboardListener: (Int) -> Unit,
    private val binding: ItemSecretKeyBinding = parent.inflateViewBinding(attachToRoot = false),
) : RecyclerView.ViewHolder(binding.root) {

    private var textWatcher: SeedPhraseWatcher? = null
    private val radius: Float = binding.textViewWord.textSize / 3
    private val filter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
    fun onBind(item: SeedPhraseWord) = with(binding) {
        binding.root.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        SeedPhraseWatcher.uninstallFrom(binding.editTextWord)
        textWatcher?.isLastKey = adapterPosition == SEED_PHRASE_SIZE_LONG

        if (item.text.isEmpty()) {
            setupKey(null)
        } else {
            setKeyCompleted(item)
        }
    }

    fun setKeyCompleted(seedPhraseWord: SeedPhraseWord) {
        val wordIndex = adapterPosition + 1
        val text = "$wordIndex ${seedPhraseWord.text}"

        if (seedPhraseWord.isValid) {
            renderValidWord(text, wordIndex)
            renderBlur(seedPhraseWord.isBlurred)
        } else {
            renderInvalidWord(text)
        }

        binding.textViewWord.setOnClickListener { onShowKeyboardListener(adapterPosition) }
        binding.textViewWord.isVisible = true
        binding.editTextWord.isVisible = false
    }

    fun setupKey(seedPhraseWord: SeedPhraseWord?) {
        with(binding) {
            textWatcher = SeedPhraseWatcher.installOn(
                isLast = adapterPosition == SEED_PHRASE_SIZE_LONG,
                editText = editTextWord,
                onKeyAdded = ::onKeyAdded,
                onSeedPhraseInserted = ::onSeedPhraseInserted
            )
            textViewWord.isVisible = false
            editTextWord.isVisible = true
            editTextWord.showSoftKeyboard()
            editTextWord.requestFocus()
            editTextWord.setOnKeyListener(::onKeyClicked)

            seedPhraseWord?.let {
                editTextWord.setText(it.text)
                editTextWord.setSelection(it.text.length)
            }
        }
    }

    fun onViewRecycled() {
        SeedPhraseWatcher.uninstallFrom(binding.editTextWord)
    }

    fun requestFocus() {
        SeedPhraseWatcher.uninstallFrom(binding.editTextWord)
        binding.editTextWord.text = null
        textWatcher = null
        setupKey(null)
    }

    private fun renderValidWord(text: String, wordIndex: Int) {
        binding.textViewWord.setTextColorRes(R.color.text_night)
        binding.textViewWord.text = SpanUtils.highlightText(
            commonText = text,
            highlightedText = wordIndex.toString(),
            color = binding.getColor(R.color.text_mountain)
        )
    }

    private fun renderBlur(isBlurred: Boolean) {
        val filter = if (isBlurred) filter else null
        binding.textViewWord.paint.maskFilter = filter
    }

    private fun renderInvalidWord(text: String) {
        binding.textViewWord.setTextColorRes(R.color.text_rose)
        binding.textViewWord.text = text
    }

    private fun onKeyClicked(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if (v !is EditText) return false

        val isDeletion = keyCode == KeyEvent.KEYCODE_DEL
        val isActionDown = event.action == KeyEvent.ACTION_DOWN
        val isEmpty = v.text.toString().isEmpty()

        if (isDeletion && isActionDown && isEmpty && adapterPosition > 0) {
            onKeyRemoved()
            return true
        }

        return false
    }

    private fun onSeedPhraseInserted(seedPhrase: List<SeedPhraseWord>) {
        binding.editTextWord.clearFocus()
        SeedPhraseWatcher.uninstallFrom(binding.editTextWord)
        binding.editTextWord.text = null
        onInsertedListener(seedPhrase)
        textWatcher = null
    }

    private fun onKeyAdded(seedPhraseWord: SeedPhraseWord) {
        SeedPhraseWatcher.uninstallFrom(binding.editTextWord)
        binding.editTextWord.text = null
        onUpdateKeyListener(seedPhraseWord)
        textWatcher = null
    }

    private fun onKeyRemoved() {
        /* We don't need to remove text change listener here, because it's already deleted when the key was added */
        onKeyRemovedListener(adapterPosition - 1)
    }
}
