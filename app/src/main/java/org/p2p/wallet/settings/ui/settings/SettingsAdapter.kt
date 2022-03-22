package org.p2p.wallet.settings.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemSettingsLogoutBinding
import org.p2p.wallet.databinding.ItemSettingsRowItemBinding
import org.p2p.wallet.databinding.ItemSettingsTitleBinding
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.utils.requireContext

class SettingsAdapter(
    private val onItemClickListener: (titleResId: Int) -> Unit,
    private val onLogoutClickListener: () -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<SettingsRow>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_settings_row_item -> {
            ViewHolder(parent, onItemClickListener)
        }
        R.layout.item_settings_logout -> {
            LogoutViewHolder(parent, onLogoutClickListener)
        }
        R.layout.item_settings_title -> {
            TitleViewHolder(parent)
        }
        else -> throw IllegalStateException("No view found for type $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bind(data[position] as SettingsRow.Section)
            is TitleViewHolder -> holder.bind(data[position] as SettingsRow.Title)
            is LogoutViewHolder -> holder.bind(data[position])
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int =
        when (data[position]) {
            is SettingsRow.Section -> R.layout.item_settings_row_item
            is SettingsRow.Title -> R.layout.item_settings_title
            is SettingsRow.Logout -> R.layout.item_settings_logout
        }

    override fun getItemCount(): Int = data.count()

    fun setData(items: List<SettingsRow>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        binding: ItemSettingsRowItemBinding,
        private val listener: (titleResId: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        constructor(
            parent: ViewGroup,
            listener: (Int) -> Unit
        ) : this(
            binding = ItemSettingsRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            listener = listener
        )

        private val titleTextView = binding.topTextView
        private val subtitleTextView = binding.bottomTextView
        private val imageView = binding.imageView
        private val bottomDivider = binding.bottomDivider

        fun bind(item: SettingsRow.Section) {
            titleTextView.setText(item.titleResId)
            item.subtitleRes?.let {
                subtitleTextView.setText(it)
            }
            item.subtitle?.let {
                subtitleTextView.text = it
            }
            item.subtitleTextColorRes?.let {
                subtitleTextView.setTextColor(requireContext().getColor(it))
            }
            imageView.setImageResource(item.iconRes)
            itemView.setOnClickListener { listener.invoke(item.titleResId) }
            bottomDivider.isVisible = item.isDivider
        }
    }

    inner class LogoutViewHolder(binding: ItemSettingsLogoutBinding, private val listener: () -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup, listener: () -> Unit) : this(
            binding = ItemSettingsLogoutBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            listener = listener
        )

        private val actionButton = binding.actionButton

        fun bind(item: SettingsRow) {
            actionButton.setOnClickListener { listener.invoke() }
        }
    }

    inner class TitleViewHolder(binding: ItemSettingsTitleBinding) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup) : this(
            ItemSettingsTitleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        private val textView = binding.topTextView
        private val topDivider = binding.topDivider

        fun bind(item: SettingsRow.Title) {
            textView.setText(item.titleResId)
            topDivider.isVisible = item.isDivider
        }
    }
}
