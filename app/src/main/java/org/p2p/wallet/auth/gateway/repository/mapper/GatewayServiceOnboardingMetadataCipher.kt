package org.p2p.wallet.auth.gateway.repository.mapper

import com.google.gson.Gson
import timber.log.Timber
import java.security.SecureRandom
import org.p2p.solanaj.utils.crypto.toBase64Instance
import org.p2p.wallet.auth.gateway.api.request.GatewayOnboardingMetadataCiphered
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.utils.chacha.ChaCha20Poly1305Decryptor
import org.p2p.wallet.utils.chacha.ChaCha20Poly1305Encryptor
import org.p2p.wallet.utils.chacha.ChaCha20Poly1305SymmetricKeyGenerator
import org.p2p.wallet.utils.fromJsonReified
import org.p2p.wallet.utils.toByteArray
import org.p2p.wallet.utils.toJsonObject

private const val NONCE_SIZE_BYTES = 12

private const val TAG = "GatewayServiceOnboardingMetadataCipher"

class GatewayServiceOnboardingMetadataCipher(
    private val chaCha20Poly1305Encryptor: ChaCha20Poly1305Encryptor,
    private val chaCha20Poly1305Decryptor: ChaCha20Poly1305Decryptor,
    private val symmetricKeyGenerator: ChaCha20Poly1305SymmetricKeyGenerator,
    private val gson: Gson
) {
    class OnboardingMetadataCipherFailed(
        override val cause: Throwable
    ) : Throwable(message = "Failed to encrypt/decrypt onboarding metadata field")

    fun encryptMetadata(
        mnemonicPhrase: List<String>,
        onboardingMetadata: GatewayOnboardingMetadata,
    ): GatewayOnboardingMetadataCiphered = try {
        val symmetricKey = symmetricKeyGenerator.generateSymmetricKey(mnemonicPhrase)
        Timber.tag(TAG).i("Symmetric key generated: ${symmetricKey.size}")

        val nonce = generateChaChaNonce()
        Timber.tag(TAG).i("Nonce generated: ${nonce.size}")

        val encryptedJson = chaCha20Poly1305Encryptor.encryptData(
            privateKey = symmetricKey,
            nonce = nonce,
            dataToEncrypt = gson.toJsonObject(onboardingMetadata).toByteArray()
        )
        Timber.tag(TAG).i("Json encrypted: ${encryptedJson.size}")

        val metadataCiphered = GatewayOnboardingMetadataCiphered(
            nonce = nonce.toBase64Instance(),
            metadataCiphered = encryptedJson.toBase64Instance(),
        )
        Timber.tag(TAG).d("Metadata generated: $metadataCiphered")

        metadataCiphered
    } catch (error: Throwable) {
        throw OnboardingMetadataCipherFailed(error)
    }

    private fun generateChaChaNonce(): ByteArray =
        ByteArray(NONCE_SIZE_BYTES)
            .also { SecureRandom().nextBytes(it) }

    fun decryptMetadata(
        mnemonicPhrase: List<String>,
        encryptedMetadata: GatewayOnboardingMetadataCiphered
    ): GatewayOnboardingMetadata = try {
        val symmetricKey = symmetricKeyGenerator.generateSymmetricKey(mnemonicPhrase)

        val decryptedJson = chaCha20Poly1305Decryptor.decryptData(
            privateKey = symmetricKey,
            nonce = encryptedMetadata.nonce.decodeToBytes(),
            dataToDecrypt = encryptedMetadata.metadataCiphered.decodeToBytes()
        )
        Timber.tag(TAG).d(String(decryptedJson))
        gson.fromJsonReified(String(decryptedJson)) ?: error("Failed to create object from $decryptedJson")
    } catch (error: Throwable) {
        throw OnboardingMetadataCipherFailed(error)
    }
}
