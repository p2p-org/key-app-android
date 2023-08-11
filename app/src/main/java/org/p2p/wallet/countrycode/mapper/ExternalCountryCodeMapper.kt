package org.p2p.wallet.countrycode.mapper

import org.p2p.wallet.countrycode.model.ExternalCountryCode
import org.p2p.wallet.countrycode.model.ExternalCountryCodeEntity

class ExternalCountryCodeMapper {

    fun fromEntity(entity: ExternalCountryCodeEntity): ExternalCountryCode = with(entity) {
        return ExternalCountryCode(
            countryName = countryName,
            nameCodeAlpha2 = nameCodeAlpha2,
            nameCodeAlpha3 = nameCodeAlpha3,
            flagEmoji = flagEmoji,
            isStrigaAllowed = isStrigaAllowed,
            isMoonpayAllowed = isMoonpayAllowed
        )
    }
}
