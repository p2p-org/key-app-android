package org.p2p.wallet.striga.sms.onramp

import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.sms.StrigaSmsApiCaller
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.striga.wallet.repository.StrigaWithdrawalsRepository

class StrigaOnRampSmsApiCaller(
    private val challengeId: StrigaWithdrawalChallengeId,
    private val strigaWithdrawalsRepository: StrigaWithdrawalsRepository
) : StrigaSmsApiCaller {
    override suspend fun resendSms(): StrigaDataLayerResult<Unit> {
        return strigaWithdrawalsRepository.resendSms(challengeId)
    }

    override suspend fun verifySms(smsCode: String): StrigaDataLayerResult<Unit> {
        return strigaWithdrawalsRepository.verifySms(smsCode, challengeId)
    }
}
