package org.p2p.wallet.home.addmoney.interactor

import org.p2p.wallet.home.addmoney.model.AddMoneyButton
import org.p2p.wallet.home.addmoney.repository.AddMoneyButtonsRepository

class AddMoneyInteractor(
    private val addMoneyButtonsRepository: AddMoneyButtonsRepository,
) {

    fun getAddMoneyButtons(): List<AddMoneyButton> = addMoneyButtonsRepository.getButtons()
}
