package org.p2p.wallet.history.repository.local.db.entities

import org.p2p.wallet.history.repository.local.db.entities.embedded.CommonTransactionInformationEntity

sealed interface TransactionEntity {
    val commonInformation: CommonTransactionInformationEntity
}
