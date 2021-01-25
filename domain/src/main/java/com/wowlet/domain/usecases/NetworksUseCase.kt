package com.wowlet.domain.usecases

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.domain.interactors.NetworksInteractor
import org.p2p.solanaj.rpc.Cluster
import org.p2p.solanaj.rpc.RpcClient

class NetworksUseCase(private val preferenceService: PreferenceService) : NetworksInteractor {

    override fun getSelectedNetwork(): Cluster = preferenceService.getSelectedCluster()

    override fun saveSelectedNetWork(cluster: Cluster) {
        preferenceService.setSelectedNetWork(cluster)
    }
}