package org.p2p.wallet.main.ui.receive.network

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentReceiveNetworkTypeBinding
import org.p2p.wallet.main.model.NetworkType
import org.p2p.wallet.renbtc.ui.main.RenBtcInfoBottomSheet
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

typealias IV = ReceiveNetworkTypeContract.View
typealias IP = ReceiveNetworkTypeContract.Presenter

private const val EXTRA_NETWORK_TYPE = "EXTRA_NETWORK_TYPE"

class ReceiveNetworkTypeFragment(private val onNetworkSelected: (NetworkType) -> Unit) :
    BaseMvpFragment<IV, IP>(R.layout.fragment_receive_network_type), IV {

    companion object {
        fun create(
            networkType: NetworkType = NetworkType.SOLANA,
            onNetworkSelected: (NetworkType) -> Unit
        ) = ReceiveNetworkTypeFragment(onNetworkSelected).withArgs(
            EXTRA_NETWORK_TYPE to networkType
        )
    }

    override val presenter: IP by inject()
    private val binding: FragmentReceiveNetworkTypeBinding by viewBinding()
    private val networkType: NetworkType by args(EXTRA_NETWORK_TYPE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
            }

            solanaButton.setOnClickListener {
                presenter.onNetworkChanged(NetworkType.SOLANA)
            }

            btcButton.setOnClickListener {
                presenter.onNetworkChanged(NetworkType.BITCOIN)
            }
            setCheckState(networkType)
        }
    }

    override fun showNetworkInfo(type: NetworkType) {
        RenBtcInfoBottomSheet.show(childFragmentManager) {
            presenter.confirm(type)
        }
        setCheckState(type)
    }

    override fun navigateToReceive(type: NetworkType) {
        onNetworkSelected.invoke(type)
        popBackStack()
    }

    private fun setCheckState(type: NetworkType) {
        with(binding) {
            solanaRadioButton.isSelected = type == NetworkType.SOLANA
            btcRadioButton.isSelected = type == NetworkType.BITCOIN
        }
    }
}