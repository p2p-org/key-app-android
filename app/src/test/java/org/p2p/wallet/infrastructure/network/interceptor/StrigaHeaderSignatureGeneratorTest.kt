package org.p2p.wallet.infrastructure.network.interceptor

import assertk.assertThat
import assertk.assertions.isTrue
import org.junit.Test
import org.p2p.solanaj.utils.TweetNaclFast
import org.p2p.core.crypto.decodeFromBase64

class StrigaHeaderSignatureGeneratorTest {
    private val signatureGenerator = StrigaHeaderSignatureGenerator()

    @Test
    fun `GIVEN invalid keypair WHEN generate THEN throw error`() {
        val invalidKeypair = ByteArray(0)

        org.junit.jupiter.api.assertThrows<TweetNaclFast.SignFailed> {
            signatureGenerator.generate(invalidKeypair)
        }
    }

    @Test
    fun `GIVEN valid keypair WHEN generate THEN signature is valid`() {
        // GIVEN
        val validKeypair = TweetNaclFast.Signature.keyPair()

        // WHEN
        val generatedSignature = signatureGenerator.generate(validKeypair.secretKey)

        // THEN
        // server-side validation
        val (timestamp, signedTimestamp) = generatedSignature.split(":")
            .run { get(0) to get(1) }
        val isVerified = TweetNaclFast.Signature(validKeypair.publicKey, byteArrayOf())
            .detached_verify(timestamp.toByteArray(), signedTimestamp.decodeFromBase64())

        assertThat(isVerified)
            .isTrue()
    }
}
