package org.p2p.wallet.newsend.ui.vialink

import kotlin.random.Random
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.newsend.model.TemporaryAccount
import org.p2p.wallet.utils.emptyString

private const val LINK_ALLOWED_SYMBOLS = "!$'()*+,-.0123456789@ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz~"
private const val SYMBOLS_COUNT = 16

object SendLinkGenerator {

    fun createTemporaryAccount(): TemporaryAccount {
        val generatedSymbols = generateSymbols()

        val account = Account.fromBip44Mnemonic(
            words = generatedSymbols,
            walletIndex = 0,
            derivationPath = DerivationPath.BIP44CHANGE,
            saltPrefix = BuildConfig.saltPrefix,
            includeSpaces = false
        )

        return TemporaryAccount(
            symbols = generatedSymbols.joinToString(emptyString()),
            address = account.publicKey.toBase58(),
            keypair = account.getEncodedKeyPair()
        )
    }

    private fun generateSymbols(): List<String> {
        val symbols = mutableListOf<String>()
        repeat(SYMBOLS_COUNT) {
            val element = LINK_ALLOWED_SYMBOLS[Random.nextInt(LINK_ALLOWED_SYMBOLS.length)].toString()
            symbols.add(element)
        }
        return symbols
    }
}
