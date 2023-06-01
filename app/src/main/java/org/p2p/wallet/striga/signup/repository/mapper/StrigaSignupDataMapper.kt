package org.p2p.wallet.striga.signup.repository.mapper

import android.content.res.Resources
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.signup.dao.StrigaSignupDataEntity
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.utils.Base58String

class StrigaSignupDataMapper(val resources: Resources) {
    fun fromEntity(entity: StrigaSignupDataEntity): StrigaSignupData {
        val type = StrigaSignupDataType.fromTag(entity.type, resources)
            ?: throw StrigaDataLayerError.InternalError(message = "Can't find any data type for ${entity.type}")

        return StrigaSignupData(
            type = type,
            value = entity.value
        )
    }

    fun toEntity(domain: StrigaSignupData, userPublicKey: Base58String): StrigaSignupDataEntity = domain.let {
        StrigaSignupDataEntity(
            type = resources.getString(it.type.tag),
            value = it.value,
            ownerPublicKey = userPublicKey
        )
    }
}
