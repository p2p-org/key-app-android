package com.p2p.wowlet.domain.usecases

import com.p2p.wowlet.datastore.PreferenceService
import org.p2p.solanaj.rpc.Cluster

class NetworksInteractor(private val preferenceService: PreferenceService) {

    fun getSelectedNetwork(): Cluster = preferenceService.getSelectedCluster()

    fun saveSelectedNetWork(cluster: Cluster) {
        preferenceService.setSelectedNetWork(cluster)
    }
}