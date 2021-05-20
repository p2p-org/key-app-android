package com.p2p.wallet.utils

import org.p2p.solanaj.core.PublicKey

fun String.toPublicKey() = PublicKey(this)