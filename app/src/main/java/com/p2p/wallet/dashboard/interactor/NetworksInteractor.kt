package com.p2p.wallet.dashboard.interactor

import com.p2p.wallet.infrastructure.persistence.PreferenceService
import org.p2p.solanaj.rpc.Cluster

class NetworksInteractor(private val preferenceService: PreferenceService) {

    fun getSelectedNetwork(): Cluster = preferenceService.getSelectedCluster()

    fun saveSelectedNetWork(cluster: Cluster) {
        preferenceService.setSelectedNetWork(cluster)
    }
}