package org.p2p.solanaj.utils

import org.p2p.solanaj.serumswap.model.Integer128
import java.math.BigInteger

fun BigInteger.toInt128(): Integer128 =
    Integer128(this)
