package org.p2p.wallet.auth.interactor

import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata

class GatewayMetadataMerger {

    fun merge(
        serverMetadata: GatewayOnboardingMetadata,
        deviceMetadata: GatewayOnboardingMetadata
    ): GatewayOnboardingMetadata {
        return if (serverMetadata.metaTimestampSec < deviceMetadata.metaTimestampSec) {
            deviceMetadata
        } else if (serverMetadata.metaTimestampSec > deviceMetadata.metaTimestampSec) {
            serverMetadata
        } else {
            val updatedDeviceNamePair = getNewerValue(
                deviceMetadata.deviceNameTimestampSec,
                serverMetadata.deviceNameTimestampSec,
                deviceMetadata.deviceShareDeviceName,
                serverMetadata.deviceShareDeviceName
            )
            val updatedPhoneNumberPair = getNewerValue(
                deviceMetadata.phoneNumberTimestampSec,
                serverMetadata.phoneNumberTimestampSec,
                deviceMetadata.customSharePhoneNumberE164,
                serverMetadata.customSharePhoneNumberE164
            )
            val updatedEmailPair = getNewerValue(
                deviceMetadata.emailTimestampSec,
                serverMetadata.emailTimestampSec,
                deviceMetadata.socialShareOwnerEmail,
                serverMetadata.socialShareOwnerEmail
            )
            val updatedStrigaMetadata =
                if (deviceMetadata.strigaMetadata != null && serverMetadata.strigaMetadata != null) {
                    getNewerValue(
                        deviceMetadata.strigaMetadata.userIdTimestamp,
                        serverMetadata.strigaMetadata.userIdTimestamp,
                        deviceMetadata.strigaMetadata,
                        serverMetadata.strigaMetadata
                    ).first
                } else {
                    deviceMetadata.strigaMetadata ?: serverMetadata.strigaMetadata
                }

            deviceMetadata.copy(
                ethPublic = deviceMetadata.ethPublic ?: serverMetadata.ethPublic,
                deviceShareDeviceName = updatedDeviceNamePair.first,
                deviceNameTimestampSec = updatedDeviceNamePair.second,
                customSharePhoneNumberE164 = updatedPhoneNumberPair.first,
                phoneNumberTimestampSec = updatedPhoneNumberPair.second,
                socialShareOwnerEmail = updatedEmailPair.first,
                emailTimestampSec = updatedEmailPair.second,
                strigaMetadata = updatedStrigaMetadata
            )
        }
    }

    private fun <T> getNewerValue(
        firstMetaValueTimestamp: Long,
        secondMetaValueTimestamp: Long,
        firstValue: T,
        secondValue: T,
    ): Pair<T, Long> {
        return if (secondMetaValueTimestamp > firstMetaValueTimestamp) {
            secondValue to secondMetaValueTimestamp
        } else {
            firstValue to firstMetaValueTimestamp
        }
    }
}
