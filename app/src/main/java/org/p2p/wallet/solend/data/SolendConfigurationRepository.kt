package org.p2p.wallet.solend.data

import org.p2p.wallet.solend.model.SolendConfiguration

interface SolendConfigurationRepository {
    suspend fun loadSolendConfiguration()
    suspend fun getSolendConfiguration(): SolendConfiguration
}
