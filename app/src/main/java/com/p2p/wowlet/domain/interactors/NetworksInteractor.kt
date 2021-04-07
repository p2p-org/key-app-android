package com.p2p.wowlet.domain.interactors

import org.p2p.solanaj.rpc.Cluster

interface NetworksInteractor {
    fun getSelectedNetwork(): Cluster

    fun saveSelectedNetWork(cluster: Cluster)
}