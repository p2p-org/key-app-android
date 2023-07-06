package org.p2p.wallet.auth.gateway.repository.mapper

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import org.junit.Test
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.utils.chacha.ChaCha20Poly1305SymmetricKeyGenerator
import org.p2p.core.crypto.toBase58Instance

class ChaCha20Poly1305SymmetricKeyGeneratorTest {
    private val expectedSymmetricKey: Base58String =
        "CDCfsZL9Xd8E2kCYYAHSeKaE16YUH9RMuXDmtTEjNC4S"
            .toBase58Instance()

    private val givenMnemonic: List<String> =
        "slice sauce assist glimpse jelly trouble parent horror bread isolate uncle gallery owner angry rose fabric stable phrase much joke cotton mesh ancient erase"
            .split(" ")

    @Test
    fun `given mnemonic when generate symmetric key multiple times then generate same symmetric key`() {
        // given
        val cha20Poly1305SymmetricKeyGenerator = ChaCha20Poly1305SymmetricKeyGenerator()
        // when
        var symmetricKey = cha20Poly1305SymmetricKeyGenerator.generateSymmetricKey(givenMnemonic)
        // then
        assertThat(symmetricKey.toBase58Instance())
            .isEqualTo(expectedSymmetricKey)

        // when 2
        symmetricKey = cha20Poly1305SymmetricKeyGenerator.generateSymmetricKey(givenMnemonic)
        // then 2
        assertThat(symmetricKey.toBase58Instance())
            .isEqualTo(expectedSymmetricKey)
    }

    @Test
    fun `given mnemonic when generate symmetric key then symmetric key should be 256 bits`() {
        // given
        val cha20Poly1305SymmetricKeyGenerator = ChaCha20Poly1305SymmetricKeyGenerator()
        // when
        val symmetricKey = cha20Poly1305SymmetricKeyGenerator.generateSymmetricKey(givenMnemonic)
        // then
        assertThat(symmetricKey)
            .hasSize(ChaCha20Poly1305SymmetricKeyGenerator.SYMMETRIC_KEY_SIZE)
    }
}
