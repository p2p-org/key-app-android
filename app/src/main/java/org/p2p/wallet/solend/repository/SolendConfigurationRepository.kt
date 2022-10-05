package org.p2p.wallet.solend.repository

import org.p2p.wallet.solend.model.SolendConfiguration

interface SolendConfigurationRepository {
    suspend fun loadSolendConfiguration()
    suspend fun getSolendConfiguration(): SolendConfiguration
}
