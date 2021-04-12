package com.p2p.wallet.dashboard.ui.dialog.sendcoins.util

import java.lang.IllegalStateException

fun String.toShortenedPublicKeyFormat(prefixLength: Int, suffixLength: Int, numberOfPoints: Int): String {
    if (this.length < prefixLength + suffixLength)
        throw IllegalStateException("Prefix and/or Suffix cant be longer than the actual string")
    val prefix: String = this.substring(0, prefixLength)
    val suffix: String = this.substring(this.length - suffixLength)
    val points: String = ".".repeat(numberOfPoints)
    return "$prefix$points$suffix"
}