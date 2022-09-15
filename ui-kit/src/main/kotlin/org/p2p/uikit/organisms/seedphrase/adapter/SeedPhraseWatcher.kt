package org.p2p.uikit.organisms.seedphrase.adapter

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import org.p2p.uikit.R
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import kotlin.properties.Delegates

/**
 * We can detect secret key addition only here
 * To detect if secret key is deleted we are using [OnKeyEditTextListener]
 * */

class SeedPhraseWatcher(
    private val onKeyAdded: (SeedPhraseWord) -> Unit,
    private val onSeedPhraseInserted: (List<SeedPhraseWord>) -> Unit,
    var isLastKey: Boolean
) : TextWatcher {

    companion object {
        fun installOn(
            isLast: Boolean,
            editText: EditText,
            onKeyAdded: (SeedPhraseWord) -> Unit,
            onSeedPhraseInserted: (List<SeedPhraseWord>) -> Unit
        ): SeedPhraseWatcher {
            val seedPhraseWatcher = SeedPhraseWatcher(onKeyAdded, onSeedPhraseInserted, isLast)
            editText.addTextChangedListener(seedPhraseWatcher)
            editText.setTag(R.id.seed_phrase_watcher_tag_id, seedPhraseWatcher)
            return seedPhraseWatcher
        }

        fun uninstallFrom(editText: EditText) {
            val seedPhraseWatcher = editText.getTag(R.id.seed_phrase_watcher_tag_id) as? SeedPhraseWatcher
            editText.removeTextChangedListener(seedPhraseWatcher)
        }
    }

    private val seedPhraseParser = SeedPhraseParser()

    private var seedPhrase: SeedPhrase by Delegates.observable(SeedPhrase(null)) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            when (val result = seedPhrase.result) {
                is KeyResult.KeyAdded -> onKeyAdded(result.key)
                is KeyResult.MultipleKeysAdded -> onSeedPhraseInserted(result.keys)
                else -> Unit
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val text = s?.toString().orEmpty()

        val keys = seedPhraseParser.parse(text)
        when (keys.size) {
            0 -> {
                Log.d("SEED_PHRASE", "User is typing and not finished yet, doing nothing $text")
            }
            1 -> {
                val result = KeyResult.KeyAdded(keys.first())
                seedPhrase = SeedPhrase(result)
            }
            else -> {
                val result = KeyResult.MultipleKeysAdded(keys)
                seedPhrase = SeedPhrase(result)
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (isLastKey) s?.clear()
    }
}

private data class SeedPhrase(
    val result: KeyResult?
)

private sealed class KeyResult {
    data class KeyAdded(val key: SeedPhraseWord) : KeyResult()
    data class MultipleKeysAdded(val keys: List<SeedPhraseWord>) : KeyResult()
}
