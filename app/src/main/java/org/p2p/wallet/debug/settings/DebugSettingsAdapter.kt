package org.p2p.wallet.debug.settings

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.requireContext
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemDebugSettingsRowItemBinding
import org.p2p.wallet.databinding.ItemSettingsDebugSwitchBinding
import org.p2p.wallet.databinding.ItemSettingsInfoBinding
import org.p2p.wallet.databinding.ItemSettingsLogoutBinding
import org.p2p.wallet.databinding.ItemSettingsTitleBinding
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.utils.withTextOrGone

class DebugSettingsAdapter(
    private val onSettingsRowClickListener: (titleResId: Int) -> Unit = {},
    private val onSettingsRowSwitchListener: (titleResId: Int, isSelected: Boolean) -> Unit = { title, isSelected -> },
    private val onLogoutClickListener: () -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<SettingsRow>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_debug_settings_row_item -> ViewHolder(parent, onSettingsRowClickListener)
        R.layout.item_settings_logout -> LogoutViewHolder(parent, onLogoutClickListener)
        R.layout.item_settings_title -> TitleViewHolder(parent)
        R.layout.item_settings_info -> InfoViewHolder(parent)
        R.layout.item_settings_debug_switch -> SwitchViewHolder(
            parent,
            onSettingsRowSwitchListener = onSettingsRowSwitchListener
        )
        else -> error("No view found for type $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bind(data[position] as SettingsRow.Section)
            is TitleViewHolder -> holder.bind(data[position] as SettingsRow.Title)
            is InfoViewHolder -> holder.bind(data[position] as SettingsRow.Info)
            is SwitchViewHolder -> holder.bind(data[position] as SettingsRow.Switcher)
            is LogoutViewHolder -> holder.bind()
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int =
        when (data[position]) {
            is SettingsRow.Section -> R.layout.item_debug_settings_row_item
            is SettingsRow.Title -> R.layout.item_settings_title
            is SettingsRow.Info -> R.layout.item_settings_info
            is SettingsRow.Logout -> R.layout.item_settings_logout
            is SettingsRow.Switcher -> R.layout.item_settings_debug_switch
            else -> error("getItemViewType failed with type in $position for ${data[position]::class.java}")
        }

    override fun getItemCount(): Int = data.count()

    fun setData(items: List<SettingsRow>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        binding: ItemDebugSettingsRowItemBinding,
        private val listener: (titleResId: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        constructor(
            parent: ViewGroup,
            listener: (Int) -> Unit
        ) : this(
            binding = ItemDebugSettingsRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            listener = listener
        )

        private val titleTextView = binding.textViewTop
        private val subtitleTextView = binding.textViewBottom
        private val imageView = binding.imageView
        private val bottomDivider = binding.bottomDivider

        fun bind(item: SettingsRow.Section) {
            titleTextView.setText(item.titleResId)
            item.subtitleRes?.let {
                subtitleTextView.setText(it)
            } ?: subtitleTextView.withTextOrGone(item.subtitle)
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
            binding = parent.inflateViewBinding<ItemSettingsLogoutBinding>(attachToRoot = false),
            listener = listener
        )

        private val actionButton = binding.actionButton

        fun bind() {
            actionButton.setOnClickListener { listener.invoke() }
        }
    }

    inner class TitleViewHolder(binding: ItemSettingsTitleBinding) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup) : this(
            parent.inflateViewBinding<ItemSettingsTitleBinding>(attachToRoot = false)
        )

        private val textView = binding.textViewTop
        private val topDivider = binding.topDivider

        fun bind(item: SettingsRow.Title) {
            textView.setText(item.titleResId)
            topDivider.isVisible = item.isDivider
        }
    }

    inner class InfoViewHolder(binding: ItemSettingsInfoBinding) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup) : this(
            parent.inflateViewBinding<ItemSettingsInfoBinding>(attachToRoot = false)
        )

        private val title = binding.titleTextView
        private val value = binding.valueTextView

        fun bind(item: SettingsRow.Info) {
            title.setText(item.titleResId)
            value.text = item.subtitle
        }
    }

    inner class SwitchViewHolder(
        parent: ViewGroup,
        binding: ItemSettingsDebugSwitchBinding = parent.inflateViewBinding(attachToRoot = false),
        private val onSettingsRowSwitchListener: (titleResId: Int, isSelected: Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val title = binding.textViewSettingsName
        private val subtitle = binding.textViewSettingsSubtitle
        private val switcher = binding.switchChangeSettings
        private val icon = binding.imageViewSettingsIcon

        fun bind(item: SettingsRow.Switcher) {
            title.setText(item.titleResId)
            subtitle.text = item.subtitle.takeIf { !it.isNullOrEmpty() }
            switcher.isChecked = item.isSelected
            icon.setImageResource(item.iconRes)
            switcher.setOnCheckedChangeListener { compoundButton, isChecked ->
                onSettingsRowSwitchListener.invoke(item.titleResId, isChecked)
            }
        }
    }
}
