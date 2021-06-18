package com.p2p.wallet.utils

import org.p2p.solanaj.model.core.PublicKey

fun String.toPublicKey() = PublicKey(this)

@Suppress("MagicNumber")
fun String.cutMiddle(): String {
    val firstSix = this.take(10)
    val lastFour = this.takeLast(10)
    return "$firstSix...$lastFour"
}