package org.p2p.wallet.solend.repository

import org.p2p.wallet.sdk.facade.SolendSdkFacade
import org.p2p.wallet.solend.repository.mapper.SolendConfigurationRepositoryMapper
import org.p2p.wallet.solend.model.SolendConfiguration
import timber.log.Timber

private const val TAG = "SolendConfigurationLocalRepository"

class SolendConfigurationLocalRepository(
    private val solendSdkFacade: SolendSdkFacade,
    private val mapper: SolendConfigurationRepositoryMapper
) : SolendConfigurationRepository {
    private var solendConfig: SolendConfiguration? = null

    override suspend fun loadSolendConfiguration() {
        this.solendConfig = mapper.fromNetwork(solendSdkFacade.getSolendConfig())
    }

    override suspend fun getSolendConfiguration(): SolendConfiguration {
        if (solendConfig == null) {
            Timber.tag(TAG).i("Solend config is not loaded. Loading now...")
            loadSolendConfiguration()
        }
        return solendConfig!!
    }
}
