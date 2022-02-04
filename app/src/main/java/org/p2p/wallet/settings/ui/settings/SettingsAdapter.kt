package org.p2p.wallet.settings.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemSettingsLogoutBinding
import org.p2p.wallet.databinding.ItemSettingsRowItemBinding
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
            ViewHolder(
                ItemSettingsRowItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                onItemClickListener
            )
        }
        R.layout.item_settings_logout -> {
            LogoutViewHolder(
                ItemSettingsLogoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                onLogoutClickListener
            )
        }
        else -> throw IllegalStateException("No view found for type $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bind(data[position])
            is LogoutViewHolder -> holder.bind(data[position])
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (data[position]) {
            is SettingsRow.Section -> R.layout.item_settings_row_item
            is SettingsRow.Title -> R.layout.item_settings_row_item
            is SettingsRow.Logout -> R.layout.item_settings_logout
        }

    override fun getItemCount(): Int = data.count()

    fun setData(items: List<SettingsRow>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    fun setSubtitleForSection(@StringRes titleResId: Int, value: String) {
    }

    inner class ViewHolder(
        binding: ItemSettingsRowItemBinding,
        private val onItemClickListener: (titleResId: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val titleTextView = binding.topTextView
        private val subtitleTextView = binding.bottomTextView
        private val imageView = binding.imageView
        private val topDivider = binding.topDivider
        private val bottomDivider = binding.bottomDivider

        fun bind(item: SettingsRow) {
            when (item) {
                is SettingsRow.Section -> bindRow(item)
                is SettingsRow.Title -> bindTitle(item)
                else -> throw IllegalStateException("Unreachable state")
            }
        }

        private fun bindRow(item: SettingsRow.Section) {
            titleTextView.setText(item.titleRes)
            if (item.subtitleRes != -1) {
                subtitleTextView.setText(item.subtitleRes)
            }
            if (item.subtitle != null) {
                subtitleTextView.text = item.subtitle
            }
            imageView.setImageResource(item.iconRes)
            itemView.setOnClickListener { onItemClickListener.invoke(item.titleRes) }
            bottomDivider.isVisible = item.isDivider
        }

        private fun bindTitle(item: SettingsRow.Title) {
            imageView.isInvisible = true
            titleTextView.setText(item.titleResId)
            titleTextView.setTextColor(requireContext().getColor(R.color.accentPrimary))
            topDivider.isVisible = item.isDivider
        }
    }

    inner class LogoutViewHolder(binding: ItemSettingsLogoutBinding, private val block: () -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        private val actionButton = binding.actionButton

        fun bind(item: SettingsRow) {
            actionButton.setOnClickListener { block.invoke() }
        }
    }
}