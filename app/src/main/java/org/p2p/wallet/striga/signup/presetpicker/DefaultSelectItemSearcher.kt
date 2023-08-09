package org.p2p.wallet.striga.signup.presetpicker

class DefaultSelectItemSearcher : SelectItemSearchEngine {
    private val searchTextMap = hashMapOf<String, List<SelectableItem>>()

    override suspend fun search(query: String, items: List<SelectableItem>): List<SelectableItem> {
        return searchTextMap[query] ?: performSearchByTitle(query, items)
    }

    private fun performSearchByTitle(query: String, items: List<SelectableItem>): List<SelectableItem> {
        val searchResult = items.filter { it.itemTitle.startsWith(query, ignoreCase = true) }
        searchTextMap[query] = searchResult
        return searchResult
    }
}
