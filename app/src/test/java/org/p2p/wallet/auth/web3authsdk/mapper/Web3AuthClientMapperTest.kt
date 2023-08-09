package org.p2p.wallet.auth.web3authsdk.mapper

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import com.google.gson.Gson
import org.junit.Test
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata

class Web3AuthClientMapperTest {

    @Test
    fun `test F iOS metadata string wrapped fix`() {
        val mapper = Web3AuthClientMapper(Gson())
        val normalMetadataJson =
            "{\"striga\":{\"user_id_timestamp\":0},\"device_name\":\"HUAWEI LYA-L29\",\"eth_public\":\"0x1187ef267d32902a120aa9e067762f65f5eb1eff\",\"email_timestamp\":0,\"auth_provider\":\"Google\",\"email\":\"test@gmail.com\",\"auth_provider_timestamp\":0,\"device_name_timestamp\":1688554023,\"phone_number\":\"+00057559974\",\"phone_number_timestamp\":0}"
        val iosMetadataJson =
            "\"{\"striga\":{\"user_id_timestamp\":0},\"device_name\":\"HUAWEI LYA-L29\",\"eth_public\":\"0x1187ef267d32902a120aa9e067762f65f5eb1eff\",\"email_timestamp\":0,\"auth_provider\":\"Google\",\"email\":\"test@gmail.com\",\"auth_provider_timestamp\":0,\"device_name_timestamp\":1688554023,\"phone_number\":\"+00057559974\",\"phone_number_timestamp\":0}\""
        val metadata1 = mapper.fromNetworkGetUserData(normalMetadataJson).getOrNull()!!
        val metadata2 = mapper.fromNetworkGetUserData(iosMetadataJson).getOrNull()
        assertThat(metadata2).all {
            isNotNull()
            prop(GatewayOnboardingMetadata::deviceShareDeviceName).isEqualTo(metadata1.deviceShareDeviceName)
            prop(GatewayOnboardingMetadata::customSharePhoneNumberE164).isEqualTo(metadata1.customSharePhoneNumberE164)
            prop(GatewayOnboardingMetadata::socialShareOwnerEmail).isEqualTo(metadata1.socialShareOwnerEmail)
            prop(GatewayOnboardingMetadata::deviceNameTimestampSec).isEqualTo(metadata1.deviceNameTimestampSec)
            prop(GatewayOnboardingMetadata::emailTimestampSec).isEqualTo(metadata1.emailTimestampSec)
            prop(GatewayOnboardingMetadata::phoneNumberTimestampSec).isEqualTo(metadata1.phoneNumberTimestampSec)
            prop(GatewayOnboardingMetadata::ethPublic).isEqualTo(metadata1.ethPublic)
            prop(GatewayOnboardingMetadata::strigaMetadata).isEqualTo(metadata1.strigaMetadata)
        }
    }
}
