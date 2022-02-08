package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection.ROW
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent.SPACE_EVENLY
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemActionButtonBinding
import org.p2p.wallet.databinding.WidgetTokenActionsBinding
import org.p2p.wallet.utils.requireContext

class ActionButtonsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = WidgetTokenActionsBinding.inflate(
        LayoutInflater.from(context), this
    )
    private val adapter = ButtonsAdapter(::onItemClicked)
    var onBuyItemClickListener: (() -> Unit)? = null
    var onReceiveItemClickListener: (() -> Unit)? = null
    var onSendClickListener: (() -> Unit)? = null
    var onSwapItemClickListener: (() -> Unit)? = null

    init {
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = ROW
            justifyContent = SPACE_EVENLY
        }
    }

    fun setItems(items: List<ActionButton>) {
        adapter.setItems(items)
    }

    private fun onItemClicked(@StringRes actionResId: Int) {
        when (actionResId) {
            R.string.main_buy -> {
                onBuyItemClickListener?.invoke()
            }
            R.string.main_receive -> {
                onReceiveItemClickListener?.invoke()
            }
            R.string.main_send -> {
                onSendClickListener?.invoke()
            }
            R.string.main_swap -> {
                onSwapItemClickListener?.invoke()
            }
        }
    }

    private class ButtonsAdapter(private val block: (Int) -> Unit) :
        RecyclerView.Adapter<ButtonsAdapter.ViewHolder>() {

        private val data = mutableListOf<ActionButton>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            ItemActionButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false), block
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int = data.size

        fun setItems(items: List<ActionButton>) {
            data.clear()
            data.addAll(items)
            notifyDataSetChanged()
        }

        inner class ViewHolder(
            binding: ItemActionButtonBinding,
            private val onItemClickListener: (Int) -> Unit
        ) : RecyclerView.ViewHolder(binding.root) {

            init {
                itemView.clipToOutline = true
            }

            val textView = binding.textView
            val imageView = binding.imageView

            fun bind(item: ActionButton) {
                textView.text = requireContext().getString(item.titleResId)
                imageView.setImageResource(item.iconResId)
                itemView.setOnClickListener { onItemClickListener.invoke(item.titleResId) }
            }
        }
    }

    data class ActionButton(@StringRes val titleResId: Int, @DrawableRes val iconResId: Int)
}