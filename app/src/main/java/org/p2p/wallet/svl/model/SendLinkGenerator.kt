package org.p2p.wallet.svl.model

import timber.log.Timber
import kotlin.random.Random
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.AccountCreationFailed
import org.p2p.solanaj.crypto.DerivationPath
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.send.model.TemporaryAccount
import org.p2p.wallet.utils.emptyString

private const val REGEX_LINK_ALLOWED_SYMBOLS = "[A-Za-z0-9_~-]"
private const val ASCII_CHARACTERS_COUNT = 128
private const val SYMBOLS_COUNT = 16

object SendLinkGenerator {

    fun createTemporaryAccount(): TemporaryAccount {
        val generatedSymbols = generateSymbols()

        Timber.tag("SendLinkGenerator").d("Generated symbols: $generatedSymbols")

        val account = try {
            Account.fromBip44Mnemonic(
                words = generatedSymbols,
                walletIndex = 0,
                derivationPath = DerivationPath.BIP44CHANGE,
                saltPrefix = BuildConfig.saltPrefix,
                includeSpaces = false
            )
        } catch (failed: AccountCreationFailed) {
            Timber.e(failed, "Failed to create temporary account")
            throw failed
        }

        Timber.tag("SendLinkGenerator").d("Account address: ${account.publicKey.toBase58()}")

        return TemporaryAccount(
            symbols = generatedSymbols.joinToString(separator = emptyString()),
            address = account.publicKey.toBase58(),
            keypair = account.getEncodedKeyPair()
        )
    }

    fun parseTemporaryAccount(seedCode: List<String>): TemporaryAccount {
        val account = Account.fromBip44Mnemonic(
            words = seedCode,
            walletIndex = 0,
            derivationPath = DerivationPath.BIP44CHANGE,
            saltPrefix = BuildConfig.saltPrefix,
            includeSpaces = false
        )

        Timber.tag("SendLinkGenerator").d("RECEIVER: Account address: ${account.publicKey.toBase58()}")

        return TemporaryAccount(
            symbols = seedCode.joinToString(emptyString()),
            address = account.publicKey.toBase58(),
            keypair = account.getEncodedKeyPair()
        )
    }

    fun isValidSeedCode(symbols: List<String>): Boolean {
        if (symbols.size < SYMBOLS_COUNT) {
            return false
        }

        val regex = Regex(REGEX_LINK_ALLOWED_SYMBOLS)
        return symbols.all { regex.matches(it) }
    }

    private fun generateSymbols(): List<String> {
        val regex = Regex(REGEX_LINK_ALLOWED_SYMBOLS)
        val random = Random.Default
        val resultSymbols = mutableListOf<String>()
        while (resultSymbols.size != SYMBOLS_COUNT) {
            val nextAsciiSymbol = Char(code = random.nextInt(ASCII_CHARACTERS_COUNT)).toString()
            if (regex.matches(nextAsciiSymbol)) {
                resultSymbols += nextAsciiSymbol
            }
        }
        return resultSymbols
    }
}
