package org.p2p.wallet.utils

fun emptyString() = ""

fun StringBuilder.appendBreakLine() {
    append("\n")
}

fun StringBuilder.appendWhitespace() {
    append(" ")
}

fun String.ellipsizeAddress(numBeforeAndAfter: Int = 4) = take(numBeforeAndAfter) + "…" + takeLast(numBeforeAndAfter)

fun String.removeWhiteSpaces(): String = replace(" ", emptyString())

fun Char.isDot(): Boolean = equals('.')
