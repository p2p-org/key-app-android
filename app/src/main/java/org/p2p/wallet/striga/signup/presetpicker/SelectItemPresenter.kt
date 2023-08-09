package org.p2p.wallet.striga.signup.presetpicker

import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.common.mvp.BasePresenter

class SelectItemPresenter(
    private val provider: SelectItemProvider,
    private val cellMapper: SelectItemPresenterCellMapper,
    private val selectedItemId: String?,
    dispatchers: CoroutineDispatchers
) : BasePresenter<SelectItemContract.View>(dispatchers.ui), SelectItemContract.Presenter {

    private var allItems: List<SelectableItem> = emptyList()

    private var selectedItem: SelectableItem? = null

    private val searchEngine: SelectItemSearchEngine = provider.searchEngine()

    override fun attach(view: SelectItemContract.View) {
        super.attach(view)
        launch {
            renderLoading()
            allItems = provider.provideItems()
            selectedItem = allItems.find { it.id == selectedItemId }
            renderAllItems()
        }
    }

    private fun renderLoading() {
        view?.showItems(cellMapper.buildLoadingCellModels())
    }

    private fun renderAllItems() {
        val cellModels = cellMapper.buildCellModels(
            items = allItems,
            selectedItem = selectedItem,
            itemName = provider.provideItemsName()
        )

        view?.showItems(cellModels)
    }

    private fun renderSearchResult(searchItems: List<SelectableItem>) {
        val cellModels = cellMapper.buildSearchCellModels(searchItems)
        view?.showItems(cellModels)
    }

    override fun onItemClicked(item: SelectableItem) {
        view?.closeWithResult(item)
    }

    override fun search(query: String) {
        launch {
            renderLoading()
            if (query.isBlank()) {
                renderAllItems()
            } else {
                renderSearchResult(searchEngine.search(query, allItems))
            }
        }
    }
}
