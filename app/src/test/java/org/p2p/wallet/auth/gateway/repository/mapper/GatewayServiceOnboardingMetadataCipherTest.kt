package org.p2p.wallet.auth.gateway.repository.mapper

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.matchesPredicate
import com.google.gson.Gson
import org.bouncycastle.crypto.modes.ChaCha20Poly1305
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
        metaTimestamp = System.currentTimeMillis(),
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
    fun `metadata migration test`() {
        // given
        val testDevice = "testDevice"
        val testPhone = "944378501"
        val testEmail = "test@gmail.com"
        val oldMetadataJson = "  {\n" +
            "    device_name: '$testDevice',\n" +
            "    phone_number: '$testPhone',\n" +
            "    email: '$testEmail',\n" +
            "    auth_provider: 'Google'\n" +
            "  }"
        val gson = Gson()
        val newMetadataStructure = gson.fromJson(oldMetadataJson, GatewayOnboardingMetadata::class.java)
        // then
        assertThat(newMetadataStructure.deviceShareDeviceName).isEqualTo(testDevice)
        assertThat(newMetadataStructure.customSharePhoneNumberE164).isEqualTo(testPhone)
        assertThat(newMetadataStructure.socialShareOwnerEmail).isEqualTo(testEmail)
        assertThat(newMetadataStructure.ethPublic).isEqualTo(null)
    }
}
