package org.p2p.wallet.debug.featuretoggles

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog
import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout.LayoutParams
import android.widget.Toast
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemSettingsToggleItemBinding
import org.p2p.wallet.utils.toDp
import org.p2p.wallet.utils.viewbinding.context
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class FeatureToggleViewHolder(
    parent: ViewGroup,
    private val onToggleChanged: (FeatureToggleRowItem, newValue: String) -> Unit,
    private val binding: ItemSettingsToggleItemBinding = parent.inflateViewBinding(attachToRoot = false),
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(itemToBind: FeatureToggleRowItem) {
        with(binding) {
            root.setOnClickListener {
                if (!itemToBind.canBeChanged) {
                    Toast.makeText(binding.context, "Switch to local to change values", Toast.LENGTH_SHORT).show()
                }
            }
            toggleKeyTextView.text = itemToBind.toggleName
            textViewIsInApp.isVisible = itemToBind.isInAppFlag

            if (itemToBind.isBooleanToggle) renderBooleanToggle(itemToBind) else renderComplexToggle(itemToBind)
        }
    }

    private fun renderBooleanToggle(item: FeatureToggleRowItem) = with(binding) {
        switchToggleValue.isVisible = true
        imageViewDetails.isVisible = false
        with(switchToggleValue) {
            setOnCheckedChangeListener(null)
            isChecked = item.toggleValue.toBoolean()
            isClickable = item.canBeChanged
            isEnabled = item.canBeChanged
            setOnCheckedChangeListener { _, isChecked -> onToggleChanged.invoke(item, isChecked.toString()) }
        }
    }

    private fun renderComplexToggle(item: FeatureToggleRowItem) = with(binding) {
        switchToggleValue.isVisible = false
        imageViewDetails.isVisible = true
        imageViewDetails.setOnClickListener { showDetailsDialog(item) }
        switchToggleValue.setOnCheckedChangeListener(null)
    }

    private fun showDetailsDialog(item: FeatureToggleRowItem) {
        val editText = EditText(binding.context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(20.toDp(), 20.toDp(), 20.toDp(), 0)
            }
            setText(item.toggleValue)
            setTextAppearance(R.style.UiKit_TextAppearance_Regular_Text1)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        AlertDialog.Builder(binding.context).apply {
            setView(editText)
            setTitle(item.toggleName)
            if (item.canBeChanged) {
                setPositiveButton("Submit") { dialog, _ ->
                    onToggleChanged.invoke(item, editText.text.toString())
                    dialog.dismiss()
                }
            }
            setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }
        }
            .show()
    }
}
