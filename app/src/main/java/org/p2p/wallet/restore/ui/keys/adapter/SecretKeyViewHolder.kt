package org.p2p.wallet.restore.ui.keys.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.showSoftKeyboard
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemSecretKeyBinding
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.SpanUtils

class SecretKeyViewHolder(
    binding: ItemSecretKeyBinding,
    private val onKeyRemovedListener: (Int) -> Unit,
    private val onUpdateKeyListener: (SecretKey) -> Unit,
    private val onInsertedListener: (List<SecretKey>) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        onKeyRemovedListener: (Int) -> Unit,
        onKeyAddedListener: (SecretKey) -> Unit,
        onInsertedListener: (List<SecretKey>) -> Unit
    ) : this(
        binding = ItemSecretKeyBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onKeyRemovedListener = onKeyRemovedListener,
        onUpdateKeyListener = onKeyAddedListener,
        onInsertedListener = onInsertedListener
    )

    private val keyTextView = binding.keyTextView
    private val keyEditText = binding.keyEditText

    private var textWatcher: SeedPhraseWatcher? = null

    @SuppressLint("SetTextI18n")
    fun onBind(item: SecretKey) {
        textWatcher?.isLastKey = bindingAdapterPosition == Constants.SEED_PHRASE_SIZE_LONG

        if (item.text.isEmpty()) {
            textWatcher = SeedPhraseWatcher.installOn(
                isLast = bindingAdapterPosition == Constants.SEED_PHRASE_SIZE_LONG,
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

    fun setKeyCompleted(secretKey: SecretKey) {
        val wordIndex = bindingAdapterPosition + 1
        val text = "$wordIndex ${secretKey.text}"

        if (secretKey.isValid) {
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

        if (isDeletion && isActionDown && isEmpty && bindingAdapterPosition > 0) {
            onKeyRemoved()
            return true
        }

        return false
    }

    private fun onSeedPhraseInserted(secretKeys: List<SecretKey>) {
        keyEditText.clearFocus()
        SeedPhraseWatcher.uninstallFrom(keyEditText)
        keyEditText.text = null
        onInsertedListener(secretKeys)
    }

    private fun onKeyAdded(secretKey: SecretKey) {
        SeedPhraseWatcher.uninstallFrom(keyEditText)
        keyEditText.text = null
        onUpdateKeyListener(secretKey)
    }

    private fun onKeyRemoved() {
        /* We don't need to remove text change listener here, because it's already deleted when the key was added */
        onKeyRemovedListener(bindingAdapterPosition - 1)
    }
}
