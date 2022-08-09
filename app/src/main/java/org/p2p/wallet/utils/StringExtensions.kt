package org.p2p.wallet.utils

fun emptyString() = ""

fun StringBuilder.appendBreakLine() {
    append("\n")
}

fun String.ellipsizeAddress() = take(4) + "…" + takeLast(4)
