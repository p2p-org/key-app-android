package org.p2p.wallet.utils

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.programs.SerumSwapProgram.usdcMint
import org.p2p.solanaj.programs.SerumSwapProgram.usdtMint

fun String.toPublicKey() = PublicKey(this)

fun PublicKey.isUsdx(): Boolean {
    val thisValue = this.toBase58()
    return thisValue == usdcMint.toBase58() || thisValue == usdtMint.toBase58()
}

@Suppress("MagicNumber")
fun String.cutMiddle(cutCount: Int = 4): String {
    val firstSix = this.take(cutCount)
    val lastSix = this.takeLast(cutCount)
    return "$firstSix...$lastSix"
}

@Suppress("MagicNumber")
fun String.cutEnd(): String {
    val firstSixteen = this.take(16)
    val lastFour = this.takeLast(4)
    return "$firstSixteen...$lastFour"
}
