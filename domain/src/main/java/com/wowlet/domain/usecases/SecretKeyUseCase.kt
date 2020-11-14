package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.domain.interactors.SecretKeyInteractor
import com.wowlet.entities.local.SecretKeyCombinationSuccess
import com.wowlet.entities.local.SecretKeyItem


class SecretKeyUseCase(val preferenceService: PreferenceService) : SecretKeyInteractor {
    private lateinit var combinationValue: SecretKeyCombinationSuccess
    private val listSortData = mutableListOf<SecretKeyItem>()
    private val selectIds = mutableListOf<Int>()
    private var currentCombination = true
    private var tempId = -1
    override suspend fun getSecretData(): List<SecretKeyItem> {
        val phraseList = preferenceService.getSecretDataAtFile()?.phrase
        phraseList?.forEachIndexed { index, value ->
            listSortData.add(SecretKeyItem(index, "${index + 1}.$value", false))
        }
        return listSortData
    }

    override fun checkCurrentSelected(id: Int): SecretKeyCombinationSuccess {

        var tempCurrentCombination = true
        combinationValue = SecretKeyCombinationSuccess(selectIds.size, tempCurrentCombination)
        selectIds.add(id)

        for (i in 0 until selectIds.size) {

            if(selectIds.size==1){
                tempCurrentCombination = true
                currentCombination = true
                tempId =  selectIds[i]
            }else if(tempId + 1 == selectIds[i]){
                tempCurrentCombination = true
                currentCombination = true
                tempId =  selectIds[i]
            }else{
                currentCombination = false
            }
        }

        combinationValue.selectedItemCount = selectIds.size
        if (selectIds.size == 3) {
            tempCurrentCombination = currentCombination
            selectIds.clear()
            currentCombination=true
            tempId=-1
        }
        combinationValue.isCurrentCombination = tempCurrentCombination
        return combinationValue
    }

}