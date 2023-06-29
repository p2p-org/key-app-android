package org.p2p.ethereumkit.internal.core.signer

import org.p2p.ethereumkit.internal.core.hexStringToByteArrayOrNull
import org.p2p.ethereumkit.internal.core.signer.Signer.PrivateKeyValidationError.InvalidDataLength
import org.p2p.ethereumkit.internal.core.signer.Signer.PrivateKeyValidationError.InvalidDataString
import org.p2p.ethereumkit.internal.crypto.TypedData
import org.p2p.ethereumkit.internal.models.*
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Mnemonic
import java.math.BigInteger
import org.p2p.core.wrapper.eth.CryptoUtils
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.core.wrapper.eth.toBigInteger

class Signer(
    private val ethSigner: EthSigner
) {

    fun signByteArray(message: ByteArray): ByteArray {
        return ethSigner.signByteArray(message)
    }

    fun signByteArrayLegacy(message: ByteArray): ByteArray {
        return ethSigner.signByteArrayLegacy(message)
    }

    fun signTypedData(rawJsonMessage: String): ByteArray {
        return ethSigner.signTypedData(rawJsonMessage)
    }

    fun parseTypedData(rawJsonMessage: String): TypedData? {
        return ethSigner.parseTypedData(rawJsonMessage)
    }

    companion object {

        fun address(
            words: List<String>,
            passphrase: String = "",
            chain: Chain
        ): EthAddress {
            return address(Mnemonic().toSeed(words, passphrase), chain)
        }

        fun address(seed: ByteArray, chain: Chain): EthAddress {
            val privateKey = privateKey(seed, chain)
            return address(privateKey)
        }

        fun privateKey(
            words: List<String>,
            passphrase: String = "",
            chain: Chain
        ): BigInteger {
            return privateKey(Mnemonic().toSeed(words, passphrase), chain)
        }

        @Throws(InvalidDataString::class, InvalidDataLength::class)
        fun privateKey(value: String): BigInteger {
            val data = value.hexStringToByteArrayOrNull()
                ?: throw InvalidDataString()
            if (data.size != 32) {
                throw InvalidDataLength()
            }
            return data.toBigInteger()
        }

        fun privateKey(seed: ByteArray, chain: Chain): BigInteger {
            val hdWallet = HDWallet(seed, chain.coinType, HDWallet.Purpose.BIP44)
            return hdWallet.privateKey(0, 0, true).privKey
        }

        fun address(privateKey: BigInteger): EthAddress {
            val publicKey =
                CryptoUtils.ecKeyFromPrivate(privateKey).publicKeyPoint.getEncoded(false).drop(1)
                    .toByteArray()
            return EthAddress(CryptoUtils.sha3(publicKey).takeLast(20).toByteArray())
        }
    }

    open class PrivateKeyValidationError : Exception() {
        class InvalidDataString : PrivateKeyValidationError()
        class InvalidDataLength : PrivateKeyValidationError()
    }
}
