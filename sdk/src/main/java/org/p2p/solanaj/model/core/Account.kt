package org.p2p.solanaj.model.core

import org.bitcoinj.crypto.DeterministicHierarchy
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.crypto.HDUtils
import org.bitcoinj.crypto.MnemonicCode
import org.p2p.solanaj.utils.TweetNaclFast

class Account {
    private var keyPair: TweetNaclFast.Signature.KeyPair

    constructor() {
        keyPair = TweetNaclFast.Signature.keyPair()
    }

    constructor(secretKey: ByteArray) {
        keyPair = TweetNaclFast.Signature.keyPair_fromSecretKey(secretKey)
    }

    private constructor(keyPair: TweetNaclFast.Signature.KeyPair) {
        this.keyPair = keyPair
    }

    val publicKey: PublicKey
        get() = PublicKey(keyPair.publicKey)

    val secretKey: ByteArray
        get() = keyPair.secretKey

    companion object {
        fun fromMnemonic(words: List<String>, passphrase: String): Account {
            val seed = MnemonicCode.toSeed(words, passphrase)
            val masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed)
            val deterministicHierarchy = DeterministicHierarchy(masterPrivateKey)
            val child = deterministicHierarchy[HDUtils.parsePath("M/44/501/0"), true, true]
            val keyPair = TweetNaclFast.Signature.keyPair_fromSeed(child.privKeyBytes)
            return Account(keyPair)
        }
    }
}