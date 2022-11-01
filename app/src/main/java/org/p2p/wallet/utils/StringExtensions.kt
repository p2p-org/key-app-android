package org.p2p.wallet.utils

fun emptyString() = ""

fun StringBuilder.appendBreakLine() {
    append("\n")
}

fun StringBuilder.appendWhitespace() {
    append(" ")
}

fun String.ellipsizeAddress() = take(4) + "…" + takeLast(4)

fun String.removeWhiteSpaces(): String = replace(" ", emptyString())

fun Char.isDot(): Boolean = equals('.')
