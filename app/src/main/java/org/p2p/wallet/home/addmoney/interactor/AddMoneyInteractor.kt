package org.p2p.wallet.home.addmoney.interactor

import org.p2p.wallet.countrycode.ExternalCountryCodeError
import org.p2p.wallet.home.addmoney.model.AddMoneyButton
import org.p2p.wallet.home.addmoney.repository.AddMoneyButtonsRepository

class AddMoneyInteractor(
    private val addMoneyButtonsRepository: AddMoneyButtonsRepository,
) {

    @Throws(ExternalCountryCodeError::class, IllegalStateException::class)
    suspend fun getAddMoneyButtons(): List<AddMoneyButton> = addMoneyButtonsRepository.getButtons()
}
