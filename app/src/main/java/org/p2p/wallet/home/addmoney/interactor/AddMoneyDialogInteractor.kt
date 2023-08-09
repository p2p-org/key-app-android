package org.p2p.wallet.home.addmoney.interactor

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.home.addmoney.repository.AddMoneyCellsRepository

class AddMoneyDialogInteractor(
    private val addMoneyCellsRepository: AddMoneyCellsRepository,
) {

    fun getAddMoneyCells(): List<AnyCellItem> = addMoneyCellsRepository.getCells()
}
