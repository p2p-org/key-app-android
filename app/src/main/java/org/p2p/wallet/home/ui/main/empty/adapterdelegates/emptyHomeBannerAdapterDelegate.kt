package org.p2p.wallet.home.ui.main.empty.adapterdelegates

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.wallet.databinding.ItemBigBannerBinding
import org.p2p.wallet.home.model.EmptyHomeItem
import org.p2p.wallet.home.model.HomeBannerItem
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

fun emptyHomeBannerAdapterDelegate(
    onBannerClicked: (buttonId: Int) -> Unit
): AdapterDelegate<List<EmptyHomeItem>> =
    adapterDelegateViewBinding<HomeBannerItem, EmptyHomeItem, ItemBigBannerBinding>(
        viewBinding = { _, parent -> parent.inflateViewBinding(attachToRoot = false) },
        block = {
            binding.buttonBanner.setOnClickListener { onBannerClicked.invoke(item.id) }
            bind {
                with(binding) {
                    textViewBannerTitle.setText(item.titleTextId)
                    textViewBannerSubtitle.setText(item.subtitleTextId)

                    imageViewBanner.setImageResource(item.drawableRes)

                    buttonBanner.apply {
                        setText(item.buttonTextId)
                    }
                }
            }
        }
    )
