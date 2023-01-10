package org.p2p.wallet.debug.featuretoggles

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup

class FeatureTogglesAdapter(
    private val onToggleChanged: (toggle: FeatureToggleRowItem, newValue: String) -> Unit,
) : RecyclerView.Adapter<FeatureToggleViewHolder>() {

    private var featureToggleRows = mutableListOf<FeatureToggleRowItem>()

    private class AdapterDiffUtil(
        private val oldList: List<FeatureToggleRowItem>,
        private val newList: List<FeatureToggleRowItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oltItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oltItem.toggleName == newItem.toggleName && oltItem.toggleValue == newItem.toggleValue
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    fun setToggleRows(newToggleRows: List<FeatureToggleRowItem>) {
        val diffResult = DiffUtil.calculateDiff(AdapterDiffUtil(featureToggleRows, newToggleRows))
        featureToggleRows.clear()
        featureToggleRows.addAll(newToggleRows)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureToggleViewHolder {
        return FeatureToggleViewHolder(parent, onToggleChanged)
    }

    override fun onBindViewHolder(holder: FeatureToggleViewHolder, position: Int) {
        holder.bind(featureToggleRows[position])
    }

    override fun getItemCount(): Int = featureToggleRows.size
}
