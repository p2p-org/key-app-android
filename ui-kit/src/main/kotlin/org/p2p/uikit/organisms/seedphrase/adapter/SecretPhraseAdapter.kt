package org.p2p.uikit.organisms.seedphrase.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.uikit.organisms.seedphrase.SeedPhraseKey
import org.p2p.uikit.organisms.seedphrase.adapter.SeedPhraseConstants.SEED_PHRASE_SIZE_LONG

class SecretPhraseAdapter(
    private val onSeedPhraseChanged: (List<SeedPhraseKey>) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<SeedPhraseKey>()

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SecretKeyViewHolder =
        SecretKeyViewHolder(
            parent = parent,
            onKeyRemovedListener = { removeSecretKey(it) },
            onKeyAddedListener = { updateSecretKey(it) },
            onInsertedListener = { addAllSecretKeys(it) }
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SecretKeyViewHolder).onBind(data[position])
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }

        payloads.forEach { data ->
            (holder as SecretKeyViewHolder).setKeyCompleted(data as SeedPhraseKey)
        }
    }

    fun updateSecretKeys(secretKeys: List<SeedPhraseKey>) {
        clear()
        addAllSecretKeys(secretKeys)
    }

    fun addSecretKey(seedPhraseKey: SeedPhraseKey) {
        data.add(seedPhraseKey)
        notifyItemInserted(data.size)
    }

    fun clear() {
        data.clear()
        data.add(SeedPhraseKey())
        notifyDataSetChanged()

        onSeedPhraseChanged(data)
    }

    fun addAllSecretKeys(seedPhrase: List<SeedPhraseKey>) {
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
        addSecretKey(SeedPhraseKey())
    }

    private fun removeSecretKey(index: Int) {
        if (index == -1) return
        data.removeAt(index)
        notifyDataSetChanged()

        onSeedPhraseChanged(data)
    }

    private fun updateSecretKey(seedPhraseKey: SeedPhraseKey) {
        /* Updating current viewHolder, where editText is active */
        val index = data.size - 1
        notifyItemChanged(index, seedPhraseKey)
        data.removeAt(index)
        data.add(index, seedPhraseKey)

        onSeedPhraseChanged(data)

        /* Adding new item, to let user continue entering seed phrase */
        addSecretKey(SeedPhraseKey())
    }
}
