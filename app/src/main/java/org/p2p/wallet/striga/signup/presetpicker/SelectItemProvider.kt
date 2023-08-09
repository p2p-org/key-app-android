package org.p2p.wallet.striga.signup.presetpicker

import androidx.recyclerview.widget.RecyclerView

interface SelectItemProvider {
    fun provideItemsName(): SelectItemsStrings

    fun provideItemDecorations(): List<RecyclerView.ItemDecoration> = emptyList()

    suspend fun provideItems(): List<SelectableItem>

    fun enableSearch(): Boolean
    fun searchEngine(): SelectItemSearchEngine = DefaultSelectItemSearcher()
}
