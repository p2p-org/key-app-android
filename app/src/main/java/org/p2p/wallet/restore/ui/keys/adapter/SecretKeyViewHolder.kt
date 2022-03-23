package org.p2p.wallet.restore.ui.keys.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemSecretKeyBinding
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.utils.showSoftKeyboard

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

    @SuppressLint("SetTextI18n")
    fun onBind(item: SecretKey) {
        if (item.text.isEmpty()) {
            SeedPhraseWatcher.installOn(
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
        val span = SpannableString("${adapterPosition + 1}. ${secretKey.text}")
        span.setSpan(
            ForegroundColorSpan(Color.BLACK),
            span.length - secretKey.text.length,
            span.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        keyTextView.text = span
        keyTextView.setBackgroundResource(R.drawable.bg_security_key)
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

    private fun onSeedPhraseInserted(secretKeys: List<SecretKey>) {
        keyEditText.clearFocus()
        SeedPhraseWatcher.uninstallFrom(keyEditText)
        onInsertedListener(secretKeys)
    }

    private fun onKeyAdded(secretKey: SecretKey) {
        SeedPhraseWatcher.uninstallFrom(keyEditText)
        onUpdateKeyListener(secretKey)
    }

    private fun onKeyRemoved() {
        /* We don't need to remove text change listener here, because it's already deleted when the key was added */
        onKeyRemovedListener(adapterPosition - 1)
    }
}
