package org.p2p.wallet.auth.gateway.repository.mapper

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isZero
import assertk.assertions.matchesPredicate
import assertk.assertions.prop
import com.google.gson.Gson
import com.google.gson.JsonElement
import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.json.JSONObject
import org.junit.Test
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.utils.chacha.ChaCha20Poly1305Decryptor
import org.p2p.wallet.utils.chacha.ChaCha20Poly1305Encryptor
import org.p2p.wallet.utils.chacha.ChaCha20Poly1305SymmetricKeyGenerator

class GatewayServiceOnboardingMetadataCipherTest {

    private val givenMnemonic: List<String> =
        "slice sauce assist glimpse jelly trouble parent horror bread isolate uncle gallery owner angry rose fabric stable phrase much joke cotton mesh ancient erase"
            .split(" ")

    private val givenGatewayMetadata: GatewayOnboardingMetadata = GatewayOnboardingMetadata(
        metaTimestampSec = System.currentTimeMillis(),
        deviceShareDeviceName = "device name",
        customSharePhoneNumberE164 = "+7111111111",
        socialShareOwnerEmail = "example@email.com"
    )

    @Test
    fun `given mnemonic and metadata when encrypt then encrypt is success`() {
        // given
        val metadataCipher = GatewayServiceOnboardingMetadataCipher(
            chaCha20Poly1305Encryptor = ChaCha20Poly1305Encryptor(ChaCha20Poly1305()),
            chaCha20Poly1305Decryptor = ChaCha20Poly1305Decryptor(ChaCha20Poly1305()),
            symmetricKeyGenerator = ChaCha20Poly1305SymmetricKeyGenerator(),
            gson = Gson()
        )
        // when
        val encryptResult = metadataCipher.encryptMetadata(givenMnemonic, givenGatewayMetadata)
        // then
        assertThat(encryptResult).all {
            matchesPredicate { it.metadataCiphered.base64Value.isNotBlank() }
            matchesPredicate { it.nonce.base64Value.isNotBlank() }
        }
    }

    @Test
    fun `given mnemonic and encrypted metadata when decrypt then decrypt is success`() {
        // given
        val metadataCipher = GatewayServiceOnboardingMetadataCipher(
            chaCha20Poly1305Encryptor = ChaCha20Poly1305Encryptor(ChaCha20Poly1305()),
            chaCha20Poly1305Decryptor = ChaCha20Poly1305Decryptor(ChaCha20Poly1305()),
            symmetricKeyGenerator = ChaCha20Poly1305SymmetricKeyGenerator(),
            gson = Gson()
        )
        val encryptedMetadata = metadataCipher.encryptMetadata(
            mnemonicPhrase = givenMnemonic,
            onboardingMetadata = givenGatewayMetadata
        )
        // when
        val actualGatewayMetadata: GatewayOnboardingMetadata = metadataCipher.decryptMetadata(
            mnemonicPhrase = givenMnemonic,
            encryptedMetadata = encryptedMetadata
        )
        // then
        assertThat(actualGatewayMetadata).isEqualTo(givenGatewayMetadata)
    }

    @Test
    fun `given old version metadata when getting it then return default fields`() {
        // given
        val oldMetadataDevice = "testDevice"
        val oldPhoneNumber = "944378501"
        val oldEmail = "test@gmail.com"
        val gson = Gson()
        val oldMetadataJson = gson.toJson(mapOf(
            "device_name" to oldMetadataDevice,
            "phone_number" to oldPhoneNumber,
            "email" to oldEmail,
            "auth_provider" to "Google"
        )).toString()
        val newMetadataStructure = gson.fromJson(oldMetadataJson, GatewayOnboardingMetadata::class.java)

        // then
        assertThat(newMetadataStructure).all {
            isNotNull()
            // old data stays the same
            prop(GatewayOnboardingMetadata::deviceShareDeviceName).isEqualTo(oldMetadataDevice)
            prop(GatewayOnboardingMetadata::customSharePhoneNumberE164).isEqualTo(oldPhoneNumber)
            prop(GatewayOnboardingMetadata::socialShareOwnerEmail).isEqualTo(oldEmail)

            // new data backs up to defaults
            prop(GatewayOnboardingMetadata::authProviderTimestampSec).isZero()
            prop(GatewayOnboardingMetadata::strigaMetadata).isNull()
            prop(GatewayOnboardingMetadata::emailTimestampSec).isZero()
            prop(GatewayOnboardingMetadata::phoneNumberTimestampSec).isZero()
            prop(GatewayOnboardingMetadata::ethPublic).isNull()
        }
    }
}
