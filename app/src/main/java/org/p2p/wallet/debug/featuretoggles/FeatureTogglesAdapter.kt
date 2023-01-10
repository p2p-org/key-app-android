package org.p2p.wallet.debug.featuretoggles

import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog
import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemSettingsToggleItemBinding
import org.p2p.wallet.utils.toDp
import org.p2p.wallet.utils.viewbinding.context
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class FeatureTogglesAdapter(
    private val onToggleChanged: (toggle: FeatureToggleRow, newValue: String) -> Unit,
) : RecyclerView.Adapter<FeatureTogglesAdapter.FeatureToggleViewHolder>() {

    private var featureToggleRows = mutableListOf<FeatureToggleRow>()

    private class AdapterDiffUtil(
        private val oldList: List<FeatureToggleRow>,
        private val newList: List<FeatureToggleRow>
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

    fun setToggleRows(newToggleRows: List<FeatureToggleRow>) {
        val diffResult = DiffUtil.calculateDiff(AdapterDiffUtil(featureToggleRows, newToggleRows))
        featureToggleRows.clear()
        featureToggleRows.addAll(newToggleRows)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureToggleViewHolder {
        return FeatureToggleViewHolder(parent.inflateViewBinding(attachToRoot = false))
    }

    override fun onBindViewHolder(holder: FeatureToggleViewHolder, position: Int) {
        holder.bind(featureToggleRows[position])
    }

    override fun getItemCount(): Int = featureToggleRows.size

    inner class FeatureToggleViewHolder(
        private val binding: ItemSettingsToggleItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(itemToBind: FeatureToggleRow) {
            with(binding) {
                toggleKeyTextView.text = itemToBind.toggleName
                textViewIsInApp.isVisible = itemToBind.isInAppFlag

                if (itemToBind.isBooleanToggle) {
                    switchToggleValue.isVisible = true
                    imageViewDetails.isVisible = false
                    switchToggleValue.setOnCheckedChangeListener(null)
                    switchToggleValue.isChecked = itemToBind.toggleValue.toBoolean()
                    switchToggleValue.isClickable = itemToBind.canBeChanged
                    switchToggleValue.isEnabled = itemToBind.canBeChanged
                    switchToggleValue.setOnCheckedChangeListener { _, isChecked ->
                        onToggleChanged.invoke(itemToBind, isChecked.toString())
                    }
                    root.setOnClickListener {
                        if (!itemToBind.canBeChanged) {
                            Toast.makeText(binding.context, "Switch to local to change values", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    switchToggleValue.isVisible = false
                    imageViewDetails.isVisible = true
                    imageViewDetails.setOnClickListener { showDetailsDialog(itemToBind) }
                    switchToggleValue.setOnCheckedChangeListener(null)
                }

            }
        }

        private fun showDetailsDialog(item: FeatureToggleRow) {
            val editText = EditText(binding.context).apply {
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(20.toDp(), 20.toDp(), 20.toDp(), 0)
                layoutParams = lp

                setText(item.toggleValue)
                setTextAppearance(R.style.UiKit_TextAppearance_Regular_Text1)
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            }

            AlertDialog.Builder(binding.context)
                .setView(editText)
                .setTitle(item.toggleName)
                .apply {
                    if (item.canBeChanged) {
                        setPositiveButton("Submit") { dialog, _ ->
                            onToggleChanged.invoke(item, editText.text.toString())
                            dialog.dismiss()
                        }
                    }
                }
                .setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
}
