package org.p2p.solanaj.core

import org.bitcoinj.crypto.DeterministicHierarchy
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.crypto.HDUtils
import org.bitcoinj.crypto.MnemonicCode
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.solanaj.crypto.SolanaBip44
import org.p2p.solanaj.utils.TweetNaclFast
import org.p2p.solanaj.utils.crypto.Base58Utils

class Account {
    private var keyPair: TweetNaclFast.Signature.KeyPair

    constructor() {
        keyPair = TweetNaclFast.Signature.keyPair()
    }

    constructor(keyPair: ByteArray) {
        this.keyPair = TweetNaclFast.Signature.keyPair_fromSecretKey(keyPair)
    }

    private constructor(keyPair: TweetNaclFast.Signature.KeyPair) {
        this.keyPair = keyPair
    }

    /**
     * Returns an encoded [keyPair] in Base58
     * */
    fun getEncodedKeyPair(): String =
        Base58Utils.encode(keypair)

    val publicKey: PublicKey
        get() = PublicKey(keyPair.publicKey)

    /**
     * The secret key is 32 byte array
     *
     * The keypair is 64 byte array which consists of encoded secret key and public key
     * */
    val keypair: ByteArray
        get() = keyPair.secretKey

    companion object {
        /**
         * Derive a Solana account from a Mnemonic generated by the Solana CLI using bip32 Mnemonic with deviation path of
         * m/501H/0H/0/0
         * @param words seed words
         * @param passphrase seed passphrase
         * @return Solana account
         */
        fun fromBip32Mnemonic(words: List<String>, walletIndex: Int, passphrase: String = ""): Account = try {
            val seed = MnemonicCode.toSeed(words, passphrase)
            val masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed)
            val deterministicHierarchy = DeterministicHierarchy(masterPrivateKey)
            val child = deterministicHierarchy[HDUtils.parsePath("M/501H/${walletIndex}H/0/0"), true, true]
            val keyPair = TweetNaclFast.Signature.keyPair_fromSeed(child.privKeyBytes)
            Account(keyPair)
        } catch (error: Throwable) {
            throw AccountCreationFailed("BIP32", error)
        }

        /**
         * Derive a Solana account from a Mnemonic generated by the Solana CLI using bip44 Mnemonic with deviation path of
         * m/44H/501H/0H
         * @param words seed words
         * @param passphrase seed passphrase
         * @return Solana account
         */
        fun fromBip44Mnemonic(words: List<String>, walletIndex: Int, passphrase: String = ""): Account = try {
            val solanaBip44 = SolanaBip44(walletIndex)
            val seed = MnemonicCode.toSeed(words, passphrase)
            val privateKey: ByteArray = solanaBip44.getPrivateKeyFromSeed(seed, DerivationPath.BIP44)
            Account(TweetNaclFast.Signature.keyPair_fromSeed(privateKey))
        } catch (error: Throwable) {
            throw AccountCreationFailed("BIP44", error)
        }

        /**
         * Derive a Solana account from a Mnemonic generated by the Solana CLI using bip44 Mnemonic with deviation path of
         * m/44H/501H/0H/0H
         * @param words seed words
         * @param passphrase seed passphrase
         * @return Solana account
         */
        fun fromBip44MnemonicWithChange(words: List<String>, walletIndex: Int, passphrase: String = ""): Account = try {
            val solanaBip44 = SolanaBip44(walletIndex)
            val seed = MnemonicCode.toSeed(words, passphrase)
            val privateKey: ByteArray = solanaBip44.getPrivateKeyFromSeed(seed, DerivationPath.BIP44CHANGE)
            Account(TweetNaclFast.Signature.keyPair_fromSeed(privateKey))
        } catch (error: Throwable) {
            throw AccountCreationFailed("BIP44-with-change", error)
        }
    }
}
