package org.p2p.wallet.common.ui.textwatcher

import org.p2p.wallet.utils.emptyString

data class PrefixData(
    val prefixText: String = emptyString(),
    val valueWithoutPrefix: String = emptyString()
)
