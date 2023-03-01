package org.p2p.ethereumkit.models

import org.p2p.ethereumkit.decorations.TransactionDecoration

class FullTransaction(
    val transaction: Transaction,
    val decoration: TransactionDecoration
)
