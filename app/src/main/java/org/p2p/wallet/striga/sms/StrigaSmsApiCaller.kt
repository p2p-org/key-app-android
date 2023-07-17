package org.p2p.wallet.striga.sms

import org.p2p.wallet.striga.common.model.StrigaDataLayerResult

interface StrigaSmsApiCaller {
    suspend fun resendSms(): StrigaDataLayerResult<Unit>
    suspend fun verifySms(smsCode: String): StrigaDataLayerResult<Unit>
}
