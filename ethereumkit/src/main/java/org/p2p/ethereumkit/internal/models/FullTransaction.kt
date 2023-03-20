package org.p2p.ethereumkit.internal.models

import org.p2p.ethereumkit.internal.decorations.TransactionDecoration

class FullTransaction(
    val transaction: Transaction,
    val decoration: TransactionDecoration
)
