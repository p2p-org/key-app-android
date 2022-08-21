package org.p2p.wallet.home.ui.main.empty.epoxy

import com.airbnb.epoxy.TypedEpoxyController
import org.p2p.wallet.home.model.EmptyHomeItem
import org.p2p.wallet.home.model.HomeBannerItem

class HomeEmptyController(
    private val onBannerClicked: (buttonId: Int) -> Unit,
) : TypedEpoxyController<List<EmptyHomeItem>>() {
    override fun buildModels(data: List<EmptyHomeItem>) {
        data.forEach { item ->
            // this forEach controls the ordinal way item are added to the list
            when (item) {
                is HomeBannerItem -> {
                    EmptyHomeBannerModel(item, onBannerClicked)
                        .id(item.id)
                        .addTo(this)
                }
                is EmptyHomeItem.EmptyHomeTitleItem -> {
                    EmptyHomeTitleModel(item)
                        .id(item.title)
                        .addTo(this)
                }
                is EmptyHomeItem.EmptyHomePopularTokensItem -> {
                    item.tokens.forEach {
                        EmptyHomePopularTokenModel(it)
                            .id(it.mintAddress)
                            .addTo(this)
                    }
                }
            }
        }
    }
}

