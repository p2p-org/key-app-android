package org.p2p.wallet.striga.sms.onramp

import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.sms.StrigaSmsApiCaller
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.striga.wallet.repository.StrigaWalletRepository

class StrigaOnRampSmsApiCaller(
    private val challengeId: StrigaWithdrawalChallengeId,
    private val strigaWalletRepository: StrigaWalletRepository,

) : StrigaSmsApiCaller {
    override suspend fun resendSms(): StrigaDataLayerResult<Unit> {
        return strigaWalletRepository.resendSms(challengeId)
    }

    override suspend fun verifySms(smsCode: String): StrigaDataLayerResult<Unit> {
        return strigaWalletRepository.verifySms(smsCode, challengeId)
    }
}
