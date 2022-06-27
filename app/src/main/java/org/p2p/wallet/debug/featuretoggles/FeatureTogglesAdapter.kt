package org.p2p.wallet.debug.featuretoggles

import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.material.switchmaterial.SwitchMaterial
import org.p2p.wallet.databinding.ItemSettingsToggleItemBinding
import org.p2p.wallet.utils.NoOp
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

    inner class FeatureToggleViewHolder(private val binding: ItemSettingsToggleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        lateinit var currentRow: FeatureToggleRow

        private val toggleValueInputWatcher = object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                text?.let { onToggleChanged.invoke(currentRow, it.toString()) }
            }

            override fun afterTextChanged(s: Editable?) = NoOp
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = NoOp
        }

        fun bind(itemToBind: FeatureToggleRow) {
            this.currentRow = itemToBind
            with(binding) {
                toggleKeyTextView.text = currentRow.toggleName
                toggleValueAsSwitch.initToggleSwitch()
                toggleValueEditText.initToggleValueInput()
            }
        }

        private fun SwitchMaterial.initToggleSwitch() {
            setOnCheckedChangeListener(null)

            isEnabled = currentRow.canBeChanged
            isInvisible = !currentRow.isCheckable
            if (isVisible) {
                isChecked = currentRow.toggleValue.toBoolean()
            }
            setOnCheckedChangeListener { _, isChecked -> onToggleChanged.invoke(currentRow, isChecked.toString()) }
        }

        private fun EditText.initToggleValueInput() {
            removeTextChangedListener(toggleValueInputWatcher)
            isClickable = currentRow.canBeChanged
            isEnabled = currentRow.canBeChanged
            setText(currentRow.toggleValue)
            addTextChangedListener(toggleValueInputWatcher)
        }
    }
}
