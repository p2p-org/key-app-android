package org.p2p.wallet.utils

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.programs.SerumSwapProgram.usdcMint
import org.p2p.solanaj.programs.SerumSwapProgram.usdtMint

const val CUT_ADDRESS_SYMBOLS_COUNT = 6
const val CUT_4_SYMBOLS = 4
const val CUT_16_SYMBOLS = 16

fun String.toPublicKey() = PublicKey(this)

fun PublicKey.isUsdx(): Boolean {
    val thisValue = this.toBase58()
    return thisValue == usdcMint.toBase58() || thisValue == usdtMint.toBase58()
}

fun String.cutMiddle(cutCount: Int = CUT_4_SYMBOLS): String {
    val firstSix = this.take(cutCount)
    val lastSix = this.takeLast(cutCount)
    return "$firstSix...$lastSix"
}

@Suppress("MagicNumber")
fun String.cutEnd(): String {
    val firstSixteen = this.take(CUT_16_SYMBOLS)
    val lastFour = this.takeLast(CUT_4_SYMBOLS)
    return "$firstSixteen...$lastFour"
}

fun String.cutStart(lastCount: Int = CUT_4_SYMBOLS): String {
    val lastSymbols = this.takeLast(lastCount)
    return "...$lastSymbols"
}
