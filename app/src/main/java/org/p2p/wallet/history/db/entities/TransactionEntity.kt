package org.p2p.wallet.history.db.entities

import org.p2p.wallet.history.db.entities.embedded.CommonTransactionInformationEntity

sealed interface TransactionEntity {
    val commonInformation: CommonTransactionInformationEntity
}
