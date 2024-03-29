package org.p2p.wallet.striga.signup.presetpicker

import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaPresetDataItem

class StrigaPresetDataSearcher {
    private val searchTextMap = hashMapOf<String, List<StrigaPresetDataItem>>()

    fun search(query: String, items: List<StrigaPresetDataItem>): List<StrigaPresetDataItem> {
        return searchTextMap[query] ?: performSearch(query, items)
    }

    private fun performSearch(query: String, items: List<StrigaPresetDataItem>): List<StrigaPresetDataItem> {
        val searchResult = items.filter { it.getName().startsWith(query, ignoreCase = true) }
        searchTextMap[query] = searchResult
        return searchResult
    }
}
