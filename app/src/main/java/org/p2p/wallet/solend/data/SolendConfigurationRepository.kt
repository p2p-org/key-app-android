package org.p2p.wallet.solend.data

import org.p2p.wallet.sdk.facade.model.SolendConfig

interface SolendConfigurationRepository {
    suspend fun loadSolendConfiguration()
    suspend fun getSolendConfiguration(): SolendConfig
}
