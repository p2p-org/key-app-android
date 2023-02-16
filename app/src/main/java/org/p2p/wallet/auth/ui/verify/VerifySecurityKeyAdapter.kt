package org.p2p.wallet.auth.ui.verify

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.p2p.uikit.utils.requireContext
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemVerifySecurityKeyBinding

@Deprecated("Old onboarding flow, delete someday")
class VerifySecurityKeyAdapter(private val block: (Int, String) -> Unit) :
    RecyclerView.Adapter<VerifySecurityKeyAdapter.ViewHolder>() {

    private val data = mutableListOf<SecurityKeyTuple>()

    fun setItems(new: List<SecurityKeyTuple>) {
        data.clear()
        data.addAll(new)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemVerifySecurityKeyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
    )

    override fun onBindViewHolder(holder: VerifySecurityKeyAdapter.ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(
        binding: ItemVerifySecurityKeyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val indexTextView = binding.indexTextView
        private val recyclerView = binding.recyclerView
        val adapter = KeysTupleAdapter()

        init {
            recyclerView.adapter = adapter
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        }

        fun bind(value: SecurityKeyTuple) {
            indexTextView.text = requireContext().getString(R.string.auth_select_word, value.index + 1)
            adapter.onItemClicked = {
                block.invoke(value.index, it)
            }
            adapter.setItems(value.keys)
        }
    }
}
