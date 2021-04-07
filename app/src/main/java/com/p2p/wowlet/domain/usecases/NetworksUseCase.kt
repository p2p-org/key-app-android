package com.p2p.wowlet.domain.usecases

import com.p2p.wowlet.datastore.PreferenceService
import com.p2p.wowlet.domain.interactors.NetworksInteractor
import org.p2p.solanaj.rpc.Cluster

class NetworksUseCase(private val preferenceService: PreferenceService) : NetworksInteractor {

    override fun getSelectedNetwork(): Cluster = preferenceService.getSelectedCluster()

    override fun saveSelectedNetWork(cluster: Cluster) {
        preferenceService.setSelectedNetWork(cluster)
    }
}