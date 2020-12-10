package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.domain.interactors.SecretKeyInteractor
import com.wowlet.entities.CallException
import com.wowlet.entities.Constants
import com.wowlet.entities.Result
import com.wowlet.entities.local.SecretKeyCombinationSuccess

class SecretKeyUseCase(
    val preferenceService: PreferenceService,
    val wowletApiCallRepository: WowletApiCallRepository
) : SecretKeyInteractor {

    private lateinit var combinationValue: SecretKeyCombinationSuccess
    private val selectIds = mutableListOf<Int>()
    private var currentCombination = true
    private var tempId = -1

    override fun checkCurrentSelected(id: Int): SecretKeyCombinationSuccess {

        var tempCurrentCombination = true
        combinationValue = SecretKeyCombinationSuccess(selectIds.size, tempCurrentCombination)
        selectIds.add(id)

        for (i in 0 until selectIds.size) {

            if (selectIds.size == 1) {
                tempCurrentCombination = true
                currentCombination = true
                tempId = selectIds[i]
            } else if (tempId + 1 == selectIds[i]) {
                tempCurrentCombination = true
                currentCombination = true
                tempId = selectIds[i]
            } else {
                currentCombination = false
            }
        }

        combinationValue.selectedItemCount = selectIds.size
        if (selectIds.size == 3) {
            tempCurrentCombination = currentCombination
            selectIds.clear()
            currentCombination = true
            tempId = -1
        }
        combinationValue.isCurrentCombination = tempCurrentCombination
        return combinationValue
    }

    override suspend fun resetPhrase(inputPhrase: String): Result<Boolean> {
        /*     val walletList = preferenceService.getWalletList()
             walletList?.let {
                 it.forEach { userData ->
                     if (userData.phrase.joinToString(separator = " ") == inputPhrase) {
                         val userAccount=wowletApiCallRepository.initAccount(userData.phrase)
                         preferenceService.updateWallet(userAccount)
                         return true
                     }
                 }
             }

                 return false*/
        return if (inputPhrase.isNotEmpty()) {
            val phrase = inputPhrase.split(" ")
            if (phrase.size==12){
                val userAccount = wowletApiCallRepository.initAccount(phrase)
                preferenceService.updateWallet(userAccount)
                Result.Success(true)
            }else{
                Result.Error(CallException(Constants.ERROR_INCORRECT_PHRASE,"Phrase size count is not 12 worlds"))
            }

        } else {
            Result.Error(CallException(Constants.ERROR_INCORRECT_PHRASE,"Phrase empty"))
        }


    }

    override fun currentPhrase(): String {
        val walletList = preferenceService.getActiveWallet()
        val phrase = walletList?.phrase?.joinToString(separator = " ")
        return phrase ?: ""
    }

}