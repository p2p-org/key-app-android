package org.p2p.wallet.solend.data.mapper

import org.p2p.wallet.sdk.facade.model.SolendConfigRootResponse
import org.p2p.wallet.solend.model.SolendConfiguration

class SolendConfigurationRepositoryMapper {
    fun fromNetwork(solendConfigResponse: SolendConfigRootResponse): SolendConfiguration {
        return SolendConfiguration()
    }
}
