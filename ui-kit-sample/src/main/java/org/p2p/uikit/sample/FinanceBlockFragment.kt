package org.p2p.uikit.sample

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import by.kirich1409.viewbindingdelegate.viewBinding
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.core.common.IconContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.atoms.icon_wrapper.IconWrapperUiModel
import org.p2p.uikit.components.finance_block.FinanceBlockUiModel
import org.p2p.uikit.components.finance_block.UiKitFinanceBlockView
import org.p2p.uikit.components.left_side.LeftSideUiModel
import org.p2p.uikit.databinding.ItemFinanceBlockBinding
import org.p2p.uikit.sample.databinding.FragmentFinanceBlockBinding
import org.p2p.uikit.utils.image.commonCircleImage
import org.p2p.uikit.utils.text.TextViewUiModel
import org.p2p.uikit.utils.toPx
import kotlin.jvm.internal.Intrinsics
import kotlin.random.Random

class FinanceBlockFragment : Fragment(R.layout.fragment_finance_block) {

    private val binding by viewBinding(FragmentFinanceBlockBinding::bind)

    private val adapter = Adapter(
        DiffCallback(),
        financeBlockDelegate(
            inflateListener = { finBlock ->
                finBlock.setOnClickAction { view, item ->
                    val rootPayload = finBlock.item.payload
                    item.payload
                    view.binding.leftSideView
                }

                finBlock.binding.rightSideView.setOnSwitchAction { view, item, isChecked ->
                    // root payload
                    val rootPayload = finBlock.item.payload
                    // inner payload
                    // item.payload
                }
            }, onBindListener = { view, item ->

            }
        ),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(view.context)
        binding.recyclerView.adapter = adapter

        fun firstText() = TextViewUiModel(
            TextContainer.Raw("teeest")
        )

        val firstIcon = commonCircleImage(
            icon = IconContainer.Res(R.drawable.ic_check),
            strokeWidth = 2f.toPx(),
            strokeColor = R.color.icons_grass,
        ).copy(
            iconTint = R.color.icons_night,
        )

        fun firstIcon() = firstIcon

        val secondIcon = commonCircleImage(
            icon = IconContainer.Res(R.drawable.ic_arrow_forward),
            strokeWidth = 2f.toPx(),
            strokeColor = R.color.icons_electric,
        ).copy(
            iconTint = R.color.icons_night,
        )

        fun secondIcon() = secondIcon

        val mock = listOf(
            IconWrapperUiModel.SingleIcon(firstIcon()),
            IconWrapperUiModel.SingleIcon(secondIcon()),
            IconWrapperUiModel.TwoIcon(firstIcon(), secondIcon()),
            IconWrapperUiModel.TwoIcon(secondIcon(), firstIcon()),
        )

        val list = mutableListOf<Any>()

        Random.nextInt(0, 4)
        list.add(
            FinanceBlockUiModel(
                LeftSideUiModel.IconWithText(firstLineText = firstText())
            )
        )
        for (i in 1..100) {
            list.add(
                FinanceBlockUiModel(
                    LeftSideUiModel.IconWithText(
                        icon = mock[Random.nextInt(0, 4)],
                        firstLineText = firstText()
                    )
                )
            )
        }

        adapter.items = list
    }
}

private class Adapter(
    diffCallback: DiffUtil.ItemCallback<Any>,
    vararg delegates: AdapterDelegate<List<Any>>,
) : AsyncListDifferDelegationAdapter<Any>(diffCallback, *delegates)

fun financeBlockDelegate(
    inflateListener: ((financeBlock: UiKitFinanceBlockView) -> Unit)? = null,
    onBindListener: ((view: UiKitFinanceBlockView, item: FinanceBlockUiModel) -> Unit)? = null,
) = adapterDelegateViewBinding<FinanceBlockUiModel, Any, ItemFinanceBlockBinding>(
    { layoutInflater, parent -> ItemFinanceBlockBinding.inflate(layoutInflater, parent, false) }
) {

    inflateListener?.invoke(binding.root)

    bind {
        binding.root.bind(item)
        onBindListener?.invoke(binding.root, item)
    }
}

private class DiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            else -> oldItem.javaClass == newItem.javaClass
        }
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            else -> Intrinsics.areEqual(oldItem, newItem)
        }
    }

    override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
        return when {
            else -> super.getChangePayload(oldItem, newItem)
        }
    }
}