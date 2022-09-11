package org.p2p.uikit.organisms.seedphrase.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.uikit.organisms.seedphrase.adapter.SeedPhraseConstants.SEED_PHRASE_SIZE_LONG

class SeedPhraseAdapter(
    private val onSeedPhraseChanged: (List<SeedPhraseWord>) -> Unit
) : RecyclerView.Adapter<SeedPhraseWordViewHolder>() {

    private val data = mutableListOf<SeedPhraseWord>()

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeedPhraseWordViewHolder =
        SeedPhraseWordViewHolder(
            parent = parent,
            onKeyRemovedListener = { removeSecretKey(it) },
            onUpdateKeyListener = { updateSecretKey(it) },
            onInsertedListener = { addAllSecretKeys(it) }
        )

    override fun onBindViewHolder(holder: SeedPhraseWordViewHolder, position: Int) {
        holder.onBind(data[position])
    }

    override fun onBindViewHolder(holder: SeedPhraseWordViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }

        payloads.forEach { data ->
            holder.setKeyCompleted(data as SeedPhraseWord)
        }
    }

    fun updateSecretKeys(secretKeys: List<SeedPhraseWord>) {
        clear()
        addAllSecretKeys(secretKeys)
    }

    fun addSecretKey(seedPhraseWord: SeedPhraseWord) {
        data.add(seedPhraseWord)
        notifyItemInserted(data.size)
    }

    fun clear() {
        data.clear()
        data.add(SeedPhraseWord.EMPTY_WORD)
        notifyDataSetChanged()

        onSeedPhraseChanged(data)
    }

    fun addAllSecretKeys(seedPhrase: List<SeedPhraseWord>) {
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
        notifyDataSetChanged()

        onSeedPhraseChanged(data)
    }

    private fun updateSecretKey(seedPhraseWord: SeedPhraseWord) {
        /* Updating current viewHolder, where editText is active */
        val index = data.size - 1
        notifyItemChanged(index, seedPhraseWord)
        data.removeAt(index)
        data.add(index, seedPhraseWord)

        onSeedPhraseChanged(data)

        /* Adding new item, to let user continue entering seed phrase */
        addSecretKey(SeedPhraseWord.EMPTY_WORD)
    }
}
