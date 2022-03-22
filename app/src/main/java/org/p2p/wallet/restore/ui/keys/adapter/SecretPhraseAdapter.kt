package org.p2p.wallet.restore.ui.keys.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.restore.model.SecretKey

class SecretPhraseAdapter(
    private val onSeedPhraseChanged: (List<SecretKey>) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<SecretKey>()

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
            (holder as SecretKeyViewHolder).setKeyCompleted(data as SecretKey)
        }
    }

    fun addSecretKey(secretKey: SecretKey) {
        data.add(secretKey)
        notifyItemInserted(data.size)
    }

    fun removeSecretKey(index: Int) {
        if (index == -1) return
        data.removeAt(index)
        notifyItemRemoved(index)

        onSeedPhraseChanged(data)
    }

    fun clear() {
        data.clear()
        data.add(SecretKey())
        notifyDataSetChanged()

        onSeedPhraseChanged(data)
    }

    private fun addAllSecretKeys(secretKeys: List<SecretKey>) {
        secretKeys.forEach { item ->
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
        addSecretKey(SecretKey())
    }

    private fun updateSecretKey(secretKey: SecretKey) {
        /* Updating current viewHolder, where editText is active */
        val index = data.size - 1
        notifyItemChanged(index, secretKey)
        data.removeAt(index)
        data.add(index, secretKey)

        onSeedPhraseChanged(data)

        /* Adding new item, to let user continue entering seed phrase */
        addSecretKey(SecretKey())
    }
}
