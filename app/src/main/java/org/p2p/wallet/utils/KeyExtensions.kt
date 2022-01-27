package org.p2p.wallet.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.programs.SerumSwapProgram.usdcMint
import org.p2p.solanaj.programs.SerumSwapProgram.usdtMint
import org.p2p.wallet.R

fun String.toPublicKey() = PublicKey(this)

fun PublicKey.isUsdx(): Boolean {
    val thisValue = this.toBase58()
    return thisValue == usdcMint.toBase58() || thisValue == usdtMint.toBase58()
}

@Suppress("MagicNumber")
fun String.cutMiddle(): String {
    val firstSix = this.take(6)
    val lastSix = this.takeLast(6)
    return "$firstSix...$lastSix"
}

@Suppress("MagicNumber")
fun String.cutEnd(): String {
    val firstSixteen = this.take(16)
    val lastFour = this.takeLast(4)
    return "$firstSixteen...$lastFour"
}

fun String.highlightCorners(context: Context): Spannable {
    val color = context.getColor(R.color.accentPrimary)
    val outPutColoredText: Spannable = SpannableString(this)
    outPutColoredText.setSpan(ForegroundColorSpan(color), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    val endIndex = length - 4
    outPutColoredText.setSpan(ForegroundColorSpan(color), endIndex, length , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return outPutColoredText
}