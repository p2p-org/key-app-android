package org.p2p.wallet.receive.token

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentReceiveTokenBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.receive.network.ReceiveNetworkTypeFragment
import org.p2p.wallet.receive.renbtc.ReceiveRenBtcFragment
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.utils.SpanUtils.highlightPublicKey
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.toast
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TOKEN = "EXTRA_TOKEN"
private const val EXTRA_IS_NETWORK_SELECTABLE = "EXTRA_IS_NETWORK_SELECTABLE"

class ReceiveTokenFragment :
    BaseMvpFragment<ReceiveTokenContract.View, ReceiveTokenContract.Presenter>(R.layout.fragment_receive_token),
    ReceiveTokenContract.View {

    companion object {
        private const val REQUEST_KEY = "REQUEST_KEY_RECEIVE_TOKEN"
        private const val BUNDLE_KEY_NETWORK_TYPE = "BUNDLE_KEY_NETWORK_TYPE"
        fun create(
            token: Token.Active,
            isNetworkSelectable: Boolean
        ) = ReceiveTokenFragment().withArgs(
            EXTRA_TOKEN to token,
            EXTRA_IS_NETWORK_SELECTABLE to isNetworkSelectable
        )
    }

    private val binding: FragmentReceiveTokenBinding by viewBinding()
    override val presenter: ReceiveTokenContract.Presenter by inject {
        parametersOf(token)
    }
    private val token: Token.Active by args(EXTRA_TOKEN)
    private val isNetworkSelectable: Boolean by args(EXTRA_IS_NETWORK_SELECTABLE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            edgeToEdge {
                toolbar.fit { Edge.Top }
                coordinator.fit { Edge.Bottom }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }
            receiveCardView.setOnNetworkClickListener {
                replaceFragment(
                    ReceiveNetworkTypeFragment.create(
                        networkType = NetworkType.SOLANA,
                        requestKey = REQUEST_KEY,
                        resultKey = BUNDLE_KEY_NETWORK_TYPE
                    )
                )
            }
            receiveCardView.setOnSaveQrClickListener { name, qrImage ->
                presenter.saveQr(name, qrImage)
            }
            receiveCardView.setQrWatermark(token.iconUrl)
            receiveCardView.showQrLoading(false)
            receiveCardView.setFaqVisibility(false)
            receiveCardView.setSelectNetworkVisibility(isNetworkSelectable)
        }
        setFragmentResultListener(REQUEST_KEY) { _, bundle ->
            val type = bundle.get(BUNDLE_KEY_NETWORK_TYPE) as NetworkType
            if (type == NetworkType.BITCOIN) {
                replaceFragment(ReceiveRenBtcFragment.create())
            }
        }
        presenter.loadData()
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        if (qrBitmap != null) {
            binding.receiveCardView.setQrBitmap(qrBitmap)
        }
    }

    override fun showReceiveToken(token: Token.Active) {
        with(binding) {
            solMintAddressTextView.text = token.mintAddress
            mintAddressView.setOnClickListener {
                requireContext().copyToClipBoard(token.mintAddress)
                toast(R.string.auth_copied)
            }
        }
    }

    override fun showUserData(userPublicKey: String, username: Username?) {
        binding.directSolAddressTextView.text = userPublicKey
        val username = username?.getFullUsername(requireContext())
        if (username != null) {
            binding.receiveCardView.setQrName(username)
            binding.receiveCardView.setQrValue(userPublicKey.highlightPublicKey(requireContext()))
            binding.directSolAddressTextView.text = userPublicKey
            binding.directSollAddressView.setOnClickListener {
                requireContext().copyToClipBoard(userPublicKey)
                toast(R.string.auth_copied)
            }
        }
    }

    override fun showFullScreenLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun showQrLoading(isLoading: Boolean) {
        binding.receiveCardView.showQrLoading(isLoading)
    }

    override fun showToastMessage(resId: Int) {
        toast(resId)
    }

    override fun showNetwork() {
        replaceFragment(
            ReceiveNetworkTypeFragment.create(
                NetworkType.SOLANA, REQUEST_KEY, BUNDLE_KEY_NETWORK_TYPE
            )
        )
    }
}