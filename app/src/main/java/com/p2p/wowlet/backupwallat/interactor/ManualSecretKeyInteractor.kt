package com.p2p.wowlet.backupwallat.interactor

import com.p2p.wowlet.common.network.CallException
import com.p2p.wowlet.common.network.Constants
import com.p2p.wowlet.common.network.Result
import com.p2p.wowlet.dashboard.model.local.SecretKeyItem

class ManualSecretKeyInteractor {

    private val sortPhraseList = mutableListOf<Int>()

    suspend fun sortingSecretKey(secretKeyItem: SecretKeyItem): Result<Boolean> {
        sortPhraseList.add(secretKeyItem.id)
        if (sortPhraseList.size == 12) {
            val containsAll = sortPhraseList == indexList()
            sortPhraseList.clear()
            return Result.Success(containsAll)
        }
        return Result.Error(CallException(Constants.REQUEST_EXACTION, ""))
    }

    private fun indexList(): List<Int> = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
}