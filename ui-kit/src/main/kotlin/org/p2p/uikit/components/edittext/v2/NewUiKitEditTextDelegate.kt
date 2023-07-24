package org.p2p.uikit.components.edittext.v2

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.databinding.ItemUiKitEditTextBinding
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.inflateViewBinding

@Suppress("UNUSED_PARAMETER")
fun newUiKitEditTextDelegate(
    inflateListener: ((financeBlock: NewUiKitEditText) -> Unit)? = null,
    onBindListener: ((view: NewUiKitEditText, item: NewUiKitEditTextCellModel) -> Unit)? = null,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<NewUiKitEditTextCellModel, AnyCellItem, ItemUiKitEditTextBinding>(
        viewBinding = { _, parent -> parent.inflateViewBinding(attachToRoot = false) },
    ) {
//        val view = binding.root
//        bind {
//            view.mutate().setText(item.text.toString())
//            if (item.isErrorState) {
//                view.mutate().setError(TextContainer("Error"))
//            }
//            item.endIcon?.let {
//                view.mutate().setDrawable(it)
//            }
//        }
    }
