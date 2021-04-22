package com.p2p.wallet.restore.interactor

import com.p2p.wallet.common.network.CallException
import com.p2p.wallet.common.network.Constants
import com.p2p.wallet.common.network.Result
import com.p2p.wallet.dashboard.model.local.SecretKeyCombinationSuccess
import com.p2p.wallet.dashboard.repository.WowletApiCallRepository
import com.p2p.wallet.infrastructure.persistence.PreferenceService
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.mnemoticgenerator.English

@Deprecated("Should be refactored")
class SecretKeyInteractor(
    val preferenceService: PreferenceService,
    val wowletApiCallRepository: WowletApiCallRepository,
    private val userInteractor: UserInteractor
) {

    private lateinit var combinationValue: SecretKeyCombinationSuccess
    private val selectIds = mutableListOf<Int>()
    private var currentCombination = true
    private var tempId = -1

    fun checkCurrentSelected(id: Int): SecretKeyCombinationSuccess {

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

    suspend fun resetPhrase(inputPhrase: String): Result<Boolean> {
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
            val words = English.INSTANCE.words
            var wordsAreValid = true
            for (word in phrase) {
                val wordNotFound = !words.contains(word)
                if (wordNotFound) {
                    wordsAreValid = false
                    break
                }
            }

            if (wordsAreValid) {
                // Temporary comparing to the list from CreateWalletUseCase class
//                CreateWalletUseCase(preferenceService, wowletApiCallRepository).generatePhrase().forEach {
//                    if (!phrase.contains(it)) {
//                        return Result.Error(CallException(Constants.ERROR_INCORRECT_PHRASE,"Phrase is invalid"))
//                    }
//
//                }

                userInteractor.createAndSaveAccount(phrase)
                Result.Success(true)
            } else {
                Result.Error(
                    CallException(
                        Constants.ERROR_INCORRECT_PHRASE,
                        "Wrong order or seed phrase, please\n" +
                            " check it and try again"
                    )
                )
            }
        } else {
            Result.Error(CallException(Constants.ERROR_INCORRECT_PHRASE, "Phrase empty"))
        }
    }

    fun currentPhrase(): String {
        val walletList = preferenceService.getActiveWallet()
        val phrase = walletList?.keys?.joinToString(separator = " ")
        return phrase ?: ""
    }

    fun currentListPhrase(): List<String> {
        return preferenceService.getActiveWallet()?.keys ?: listOf()
    }
}