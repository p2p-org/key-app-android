package com.wowlet.domain.usecases

import com.wowlet.domain.interactors.ManualSecretKeyInteractor
import com.wowlet.entities.CallException
import com.wowlet.entities.Constants
import com.wowlet.entities.Result
import com.wowlet.entities.local.SecretKeyItem

class ManualSecretKeyUseCase() : ManualSecretKeyInteractor {

    private val sortPhraseList = mutableListOf<Int>()

    override suspend fun sortingSecretKey(secretKeyItem: SecretKeyItem): Result<Boolean> {
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