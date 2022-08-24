package org.p2p.uikit.organisms.seedphrase.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import org.p2p.uikit.R
import org.p2p.uikit.databinding.ItemSecretKeyBinding
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.showSoftKeyboard
import org.p2p.uikit.organisms.seedphrase.SeedPhraseKey
import org.p2p.uikit.organisms.seedphrase.adapter.SeedPhraseConstants.SEED_PHRASE_SIZE_LONG
import org.p2p.uikit.utils.SpanUtils

class SecretKeyViewHolder(
    binding: ItemSecretKeyBinding,
    private val onKeyRemovedListener: (Int) -> Unit,
    private val onUpdateKeyListener: (SeedPhraseKey) -> Unit,
    private val onInsertedListener: (List<SeedPhraseKey>) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        onKeyRemovedListener: (Int) -> Unit,
        onKeyAddedListener: (SeedPhraseKey) -> Unit,
        onInsertedListener: (List<SeedPhraseKey>) -> Unit
    ) : this(
        binding = ItemSecretKeyBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onKeyRemovedListener = onKeyRemovedListener,
        onUpdateKeyListener = onKeyAddedListener,
        onInsertedListener = onInsertedListener
    )

    private val keyTextView = binding.keyTextView
    private val keyEditText = binding.keyEditText

    private var textWatcher: SeedPhraseWatcher? = null

    fun onBind(item: SeedPhraseKey) {
        textWatcher?.isLastKey = adapterPosition == SEED_PHRASE_SIZE_LONG

        if (item.text.isEmpty()) {
            textWatcher = SeedPhraseWatcher.installOn(
                isLast = adapterPosition == SEED_PHRASE_SIZE_LONG,
                editText = keyEditText,
                onKeyAdded = { onKeyAdded(it) },
                onSeedPhraseInserted = { onSeedPhraseInserted(it) }
            )
            keyTextView.isVisible = false
            keyEditText.isVisible = true
            keyEditText.showSoftKeyboard()
            keyEditText.requestFocus()
            keyEditText.setOnKeyListener { v, keyCode, event ->
                return@setOnKeyListener onKeyClicked(v, keyCode, event)
            }
        } else {
            setKeyCompleted(item)
        }
    }

    fun setKeyCompleted(seedPhraseKey: SeedPhraseKey) {
        val wordIndex = adapterPosition + 1
        val text = "$wordIndex ${seedPhraseKey.text}"

        if (seedPhraseKey.isValid) {
            keyTextView.setTextColor(itemView.getColor(R.color.night))
            val color = itemView.getColor(R.color.mountain)
            val spannedText = SpanUtils.highlightText(text, "$wordIndex", color)
            keyTextView.text = spannedText
        } else {
            keyTextView.setTextColor(itemView.getColor(R.color.rose))
            keyTextView.text = text
        }

        keyTextView.isVisible = true
        keyEditText.isVisible = false
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

    private fun onSeedPhraseInserted(seedPhrase: List<SeedPhraseKey>) {
        keyEditText.clearFocus()
        SeedPhraseWatcher.uninstallFrom(keyEditText)
        keyEditText.text = null
        onInsertedListener(seedPhrase)
    }

    private fun onKeyAdded(seedPhraseKey: SeedPhraseKey) {
        SeedPhraseWatcher.uninstallFrom(keyEditText)
        keyEditText.text = null
        onUpdateKeyListener(seedPhraseKey)
    }

    private fun onKeyRemoved() {
        /* We don't need to remove text change listener here, because it's already deleted when the key was added */
        onKeyRemovedListener(adapterPosition - 1)
    }
}
