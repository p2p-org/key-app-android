package org.p2p.uikit.organisms.seedphrase.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.uikit.organisms.seedphrase.adapter.SeedPhraseConstants.SEED_PHRASE_SIZE_LONG

class SeedPhraseAdapter(
    private val onSeedPhraseChanged: (List<SeedPhraseWord>) -> Unit,
    private val onShowKeyboardListener: (Int) -> Unit,
) : RecyclerView.Adapter<SeedPhraseWordViewHolder>() {

    private val data = mutableListOf<SeedPhraseWord>()

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeedPhraseWordViewHolder =
        SeedPhraseWordViewHolder(
            parent = parent,
            onKeyRemovedListener = { removeSecretKey(it) },
            onUpdateKeyListener = { updateSecretKey(it) },
            onInsertedListener = { addAllSecretKeys(it) },
            onShowKeyboardListener = onShowKeyboardListener
        )

    override fun onBindViewHolder(holder: SeedPhraseWordViewHolder, position: Int) {
        holder.onBind(data[position])
    }

    override fun onBindViewHolder(holder: SeedPhraseWordViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }

        payloads.forEach { payload ->
            when (payload) {
                is SeedPhraseWord -> holder.setKeyCompleted(payload)
                is Int -> holder.requestFocus()
            }
        }
    }

    fun replaceSecretKeys(seedPhrase: List<SeedPhraseWord>) {
        clear()
        addAllSecretKeys(seedPhrase)
    }

    fun addSecretKey(seedPhraseWord: SeedPhraseWord) {
        if (data.size >= SEED_PHRASE_SIZE_LONG) {
            return
        }

        data.add(seedPhraseWord)
        notifyItemInserted(data.size)
    }

    fun showFocusOnLastItem() {
        val lastItem = data.lastOrNull()

        if (lastItem == null || lastItem.text.isNotEmpty()) {
            addSecretKey(SeedPhraseWord.EMPTY_WORD)
            return
        }

        if (lastItem.text.isEmpty()) {
            val itemIndex = data.lastIndex
            notifyItemChanged(itemIndex, itemIndex)
        }
    }

    fun clear() {
        val dataSize = data.size
        data.clear()
        notifyItemRangeRemoved(0, dataSize)
        data.add(SeedPhraseWord.EMPTY_WORD)
        notifyItemInserted(data.size - 1)

        onSeedPhraseChanged(data)
    }

    fun addAllSecretKeys(seedPhrase: List<SeedPhraseWord>) {
        if (data.size >= SEED_PHRASE_SIZE_LONG) {
            return
        }

        seedPhrase
            .take(SEED_PHRASE_SIZE_LONG)
            .forEach { item ->
                /* If there is empty element exists, then we are updating it to the first entered key */
                val index = data.indexOfFirst { it.text.isEmpty() }
                if (index != -1) {
                    /* First we update the UI and then updating the actual data */
                    notifyItemChanged(index, item)
                    data.removeAt(index)
                    data.add(index, item)
                } else {
                    /* If there is no empty elements, then this is a new item */
                    data.add(item)
                    notifyItemInserted(data.size - 1)
                }
            }

        onSeedPhraseChanged(data)

        /* Automatically adding new empty element, so user could continue entering the seed phrase */
        addSecretKey(SeedPhraseWord.EMPTY_WORD)
    }

    private fun removeSecretKey(index: Int) {
        if (index == -1) return
        data.removeAt(index)
        notifyItemRemoved(index)

        onSeedPhraseChanged(data)
    }

    private fun updateSecretKey(seedPhraseWord: SeedPhraseWord) {
        /* Updating current viewHolder, where editText is active */
        val index = data.size - 1
        data[index] = seedPhraseWord
        notifyItemChanged(index, seedPhraseWord)

        onSeedPhraseChanged(data)

        /* Adding new item, to let user continue entering seed phrase */
        addSecretKey(SeedPhraseWord.EMPTY_WORD)
    }
}
