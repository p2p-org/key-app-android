package org.p2p.wallet.auth.gateway.repository.mapper

import com.google.gson.Gson
import org.p2p.solanaj.crypto.SolanaBip44Custom
import org.p2p.solanaj.utils.crypto.toBase64Instance
import org.p2p.solanaj.utils.crypto.toBase64String
import org.p2p.wallet.auth.gateway.api.request.GatewayOnboardingMetadataCiphered
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.utils.fromJsonReified
import org.p2p.wallet.utils.toByteArray
import org.p2p.wallet.utils.toJsonObject
import timber.log.Timber
import java.security.SecureRandom

private const val NONCE_SIZE_BYTES = 12

private const val TAG = "GatewayServiceOnboardingMetadataCipher"

// only for iOS
private const val AUTH_TAG_ANDROID_VALUE = "no_value"

class GatewayServiceOnboardingMetadataCipher(
    private val chaCha20Poly1305Encryptor: ChaCha20Poly1305Encryptor,
    private val chaCha20Poly1305Decryptor: ChaCha20Poly1305Decryptor,
    private val gson: Gson
) {
    class OnboardingMetadataCipherFailed(
        override val cause: Throwable
    ) : Throwable(message = "Failed to encrypt/decrypt onboarding metadata field")

    private val chaChaPrivateKeyGenerator = SolanaBip44Custom(
        purpose = 44,
        type = 101,
        account = 0,
        change = 0,
        isHardened = false
    )

    fun encryptMetadata(
        mnemonicPhrase: List<String>,
        onboardingMetadata: GatewayOnboardingMetadata,
    ): GatewayOnboardingMetadataCiphered = try {
        val seedPhraseInBytes = mnemonicPhrase.joinToString(" ").toByteArray()
        val privateKey = generateChaChaPrivateKey(seedPhraseInBytes)
        Timber.tag(TAG).i("Private key generated: ${privateKey.size}")

        val nonce = generateChaChaNonce()
        Timber.tag(TAG).i("Nonce generated: ${nonce.size}")

        val encryptedJson = chaCha20Poly1305Encryptor.encryptData(
            privateKey = privateKey,
            nonce = nonce,
            dataToEncrypt = gson.toJsonObject(onboardingMetadata).toByteArray()
        )
        Timber.tag(TAG).i("Json encrypted: ${encryptedJson.size}")

        val metadataCiphered = GatewayOnboardingMetadataCiphered(
            nonce = nonce.toBase64Instance(),
            metadataCiphered = encryptedJson.toBase64Instance(),
            tag = AUTH_TAG_ANDROID_VALUE.toBase64String()
        )
        Timber.tag(TAG).d("Metadata generated: $metadataCiphered")

        metadataCiphered
    } catch (error: Throwable) {
        throw OnboardingMetadataCipherFailed(error)
    }

    /**
     * derivation path used: m/44'/101'/0'/0'
     */
    private fun generateChaChaPrivateKey(seedPhraseBytes: ByteArray): ByteArray =
        chaChaPrivateKeyGenerator.getPrivateKeyFromSeed(seedPhraseBytes)

    private fun generateChaChaNonce(): ByteArray =
        ByteArray(NONCE_SIZE_BYTES)
            .also { SecureRandom().nextBytes(it) }

    fun decryptMetadata(
        mnemonicPhrase: List<String>,
        encryptedMetadata: GatewayOnboardingMetadataCiphered
    ): GatewayOnboardingMetadata = try {
        val seedPhraseInBytes = mnemonicPhrase.joinToString(" ").toByteArray()
        val privateKey = generateChaChaPrivateKey(seedPhraseInBytes)

        val decryptedJson = chaCha20Poly1305Decryptor.decryptData(
            privateKey = privateKey,
            nonce = encryptedMetadata.nonce.decodeToBytes(),
            dataToDecrypt = encryptedMetadata.metadataCiphered.decodeToBytes()
        )
        gson.fromJsonReified(String(decryptedJson)) ?: error("Failed to create object from $decryptedJson")
    } catch (error: Throwable) {
        throw OnboardingMetadataCipherFailed(error)
    }
}
