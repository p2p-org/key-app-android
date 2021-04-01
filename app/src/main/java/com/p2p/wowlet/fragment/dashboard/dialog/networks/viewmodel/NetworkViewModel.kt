package com.p2p.wowlet.fragment.dashboard.dialog.networks.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.NetworksInteractor
import org.p2p.solanaj.rpc.Cluster

class NetworkViewModel(private val networksInteractor: NetworksInteractor) : BaseViewModel() {

    private val _getSelectedNetwork by lazy { MutableLiveData<Cluster>() }
    val getSelectedNetwork: LiveData<Cluster> = _getSelectedNetwork

    fun getSelectedNetwork() {
        val selectedNetwork = networksInteractor.getSelectedNetwork()
        _getSelectedNetwork.value = selectedNetwork
    }

    fun saveSelectedNetwork(cluster: Cluster) {
        networksInteractor.saveSelectedNetWork(cluster)
    }

    fun getSelectedNetworkName() : String {
        getSelectedNetwork()
        return when(getSelectedNetwork.value) {
            Cluster.DEVNET -> "Dev"
            Cluster.MAINNET -> "Main"
            Cluster.TESTNET -> "Test"
            Cluster.SOLANANET -> "Solana"
            else -> "Solana"
        }
    }

}