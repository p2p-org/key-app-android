package org.p2p.wallet.solend.data

import org.p2p.wallet.sdk.facade.SolendSdkFacade
import org.p2p.wallet.sdk.facade.model.SolendConfig
import timber.log.Timber

private const val TAG = "SolendConfigurationLocalRepository"

class SolendConfigurationLocalRepository(
    private val solendSdkFacade: SolendSdkFacade
) : SolendConfigurationRepository {
    private var solendConfig: SolendConfig? = null

    override suspend fun loadSolendConfiguration() {
        this.solendConfig = solendSdkFacade.getSolendConfig().solendConfig
    }

    override suspend fun getSolendConfiguration(): SolendConfig {
        if (solendConfig == null) {
            Timber.tag(TAG).i("Solend config is not loaded. Loading now...")
            loadSolendConfiguration()
        }
        return solendConfig!!
    }
}
