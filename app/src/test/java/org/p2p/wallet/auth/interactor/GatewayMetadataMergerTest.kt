package org.p2p.wallet.auth.interactor

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import org.junit.Test
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.utils.DateTimeUtils

internal class GatewayMetadataMergerTest {

    private val metadataMerger = GatewayMetadataMerger()
    private val oldTimeSeconds = DateTimeUtils.getCurrentTimestampInSeconds()
    private val newTimeSeconds = oldTimeSeconds + 100

    private val oldMetadataDevice = "testDevice"
    private val oldPhoneNumber = "944378501"
    private val oldEmail = "test@gmail.com"
    private val oldEthAddress = "0x1232157465145186"

    private val newMetadataDevice = "newTestDevice"
    private val newPhoneNumber = "104378501"
    private val newEmail = "newtest@gmail.com"
    private val newEthAddress = "0x111111111111111"
    private val newStrigaMetadata = GatewayOnboardingMetadata.StrigaMetadata(
        userId = "testStrigaId",
        userIdTimestamp = newTimeSeconds
    )

    @Test
    fun `given device metadata and server metadata with different metaTimestampSec then return metadata with updated fields`() {
        // given
        val deviceMetadata = GatewayOnboardingMetadata(
            deviceShareDeviceName = oldMetadataDevice,
            customSharePhoneNumberE164 = oldPhoneNumber,
            socialShareOwnerEmail = oldEmail,
            ethPublic = oldEthAddress,
            metaTimestampSec = oldTimeSeconds,
            deviceNameTimestampSec = oldTimeSeconds,
            phoneNumberTimestampSec = oldTimeSeconds,
            emailTimestampSec = oldTimeSeconds,
            authProviderTimestampSec = oldTimeSeconds
        )
        val serverMetadata = GatewayOnboardingMetadata(
            deviceShareDeviceName = newMetadataDevice,
            customSharePhoneNumberE164 = newPhoneNumber,
            socialShareOwnerEmail = newEmail,
            ethPublic = newEthAddress,
            metaTimestampSec = newTimeSeconds,
            deviceNameTimestampSec = newTimeSeconds,
            phoneNumberTimestampSec = newTimeSeconds,
            emailTimestampSec = newTimeSeconds,
            authProviderTimestampSec = newTimeSeconds,
            strigaMetadata = newStrigaMetadata
        )

        val mergedMetadata = metadataMerger.merge(serverMetadata, deviceMetadata)
        // then
        assertThat(mergedMetadata).all {
            isNotNull()
            this.isEqualTo(serverMetadata)
            prop(GatewayOnboardingMetadata::deviceShareDeviceName).isEqualTo(newMetadataDevice)
            prop(GatewayOnboardingMetadata::customSharePhoneNumberE164).isEqualTo(newPhoneNumber)
            prop(GatewayOnboardingMetadata::socialShareOwnerEmail).isEqualTo(newEmail)
            prop(GatewayOnboardingMetadata::authProviderTimestampSec).isEqualTo(newTimeSeconds)
            prop(GatewayOnboardingMetadata::deviceNameTimestampSec).isEqualTo(newTimeSeconds)
            prop(GatewayOnboardingMetadata::emailTimestampSec).isEqualTo(newTimeSeconds)
            prop(GatewayOnboardingMetadata::phoneNumberTimestampSec).isEqualTo(newTimeSeconds)
            prop(GatewayOnboardingMetadata::ethPublic).isEqualTo(newEthAddress)
            prop(GatewayOnboardingMetadata::strigaMetadata).isEqualTo(newStrigaMetadata)
        }
    }

    @Test
    fun `given device metadata and server metadata with same metaTimestampSec then return metadata with updated fields`() {
        // given
        val deviceMetadata = GatewayOnboardingMetadata(
            deviceShareDeviceName = oldMetadataDevice,
            customSharePhoneNumberE164 = oldPhoneNumber,
            socialShareOwnerEmail = oldEmail,
            ethPublic = oldEthAddress,
            metaTimestampSec = oldTimeSeconds,
            deviceNameTimestampSec = oldTimeSeconds,
            phoneNumberTimestampSec = newTimeSeconds,
            emailTimestampSec = newTimeSeconds,
            authProviderTimestampSec = oldTimeSeconds
        )
        val serverMetadata = GatewayOnboardingMetadata(
            deviceShareDeviceName = newMetadataDevice,
            customSharePhoneNumberE164 = newPhoneNumber,
            socialShareOwnerEmail = newEmail,
            ethPublic = newEthAddress,
            metaTimestampSec = oldTimeSeconds,
            deviceNameTimestampSec = newTimeSeconds,
            phoneNumberTimestampSec = oldTimeSeconds,
            emailTimestampSec = oldTimeSeconds,
            authProviderTimestampSec = newTimeSeconds,
            strigaMetadata = newStrigaMetadata
        )

        val mergedMetadata = metadataMerger.merge(serverMetadata, deviceMetadata)
        // then
        assertThat(mergedMetadata).all {
            isNotNull()
            this.isNotEqualTo(serverMetadata)
            this.isNotEqualTo(deviceMetadata)
            prop(GatewayOnboardingMetadata::deviceShareDeviceName).isEqualTo(newMetadataDevice)
            prop(GatewayOnboardingMetadata::customSharePhoneNumberE164).isEqualTo(oldPhoneNumber)
            prop(GatewayOnboardingMetadata::socialShareOwnerEmail).isEqualTo(oldEmail)
            prop(GatewayOnboardingMetadata::deviceNameTimestampSec).isEqualTo(newTimeSeconds)
            prop(GatewayOnboardingMetadata::emailTimestampSec).isEqualTo(newTimeSeconds)
            prop(GatewayOnboardingMetadata::phoneNumberTimestampSec).isEqualTo(newTimeSeconds)
            prop(GatewayOnboardingMetadata::ethPublic).isEqualTo(oldEthAddress)
            prop(GatewayOnboardingMetadata::strigaMetadata).isEqualTo(newStrigaMetadata)
        }
    }
}
