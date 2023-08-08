package org.p2p.wallet.settings.mapper

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.settings.model.UserCountrySettingsEntity

class SettingsEntityMapper {

    fun toEntity(data: CountryCode): UserCountrySettingsEntity = with(data) {
        return UserCountrySettingsEntity(
            name = countryName,
            codeAlpha2 = nameCodeAlpha2,
            codeAlpha3 = nameCodeAlpha3,
            phoneCode = phoneCode,
            phoneMask = mask,
            flagEmoji = flagEmoji
        )
    }

    fun fromEntity(entity: UserCountrySettingsEntity): CountryCode = with(entity) {
        return CountryCode(
            nameCodeAlpha2 = codeAlpha2,
            nameCodeAlpha3 = codeAlpha3,
            phoneCode = phoneCode,
            countryName = name,
            flagEmoji = flagEmoji,
            mask = phoneMask
        )
    }
}
