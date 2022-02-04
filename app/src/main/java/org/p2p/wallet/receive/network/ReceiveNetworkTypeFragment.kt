package org.p2p.wallet.receive.network

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentReceiveNetworkTypeBinding
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.renbtc.ui.main.RenBtcInfoBottomSheet
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_NETWORK_TYPE = "EXTRA_NETWORK_TYPE"

class ReceiveNetworkTypeFragment() :
    BaseMvpFragment<ReceiveNetworkTypeContract.View, ReceiveNetworkTypeContract.Presenter>
    (R.layout.fragment_receive_network_type),
    ReceiveNetworkTypeContract.View {

    companion object {
        const val REQUEST_KEY = "REQUEST_KEY_NETWORK_TYPE"
        const val BUNDLE_NETWORK_KEY = "BUNDLE_NETWORK_KEY"

        fun create(
            networkType: NetworkType = NetworkType.SOLANA
        ) = ReceiveNetworkTypeFragment().withArgs(
            EXTRA_NETWORK_TYPE to networkType
        )
    }

    override val presenter: ReceiveNetworkTypeContract.Presenter by inject {
        parametersOf(networkType)
    }
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
        }
        presenter.load()
    }

    override fun showNetworkInfo(type: NetworkType) {
        RenBtcInfoBottomSheet.show(childFragmentManager) {
            navigateToReceive(type)
        }
    }

    override fun setCheckState(type: NetworkType) {
        with(binding) {
            solanaRadioButton.isSelected = type == NetworkType.SOLANA
            btcRadioButton.isSelected = type == NetworkType.BITCOIN
        }
    }

    override fun navigateToReceive(type: NetworkType) {
        setFragmentResult(REQUEST_KEY, Bundle().apply { putParcelable(BUNDLE_NETWORK_KEY, type) })
        popBackStack()
    }
}