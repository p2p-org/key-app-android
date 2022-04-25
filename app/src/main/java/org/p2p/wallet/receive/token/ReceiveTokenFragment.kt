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
import org.p2p.wallet.utils.SpanUtils
import org.p2p.wallet.utils.SpanUtils.highlightPublicKey
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.shareScreenShot
import org.p2p.wallet.utils.toast
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.io.File

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

class ReceiveTokenFragment :
    BaseMvpFragment<ReceiveTokenContract.View, ReceiveTokenContract.Presenter>(R.layout.fragment_receive_token),
    ReceiveTokenContract.View {

    companion object {
        private const val REQUEST_KEY = "REQUEST_KEY_RECEIVE_TOKEN"
        private const val BUNDLE_KEY_NETWORK_TYPE = "BUNDLE_KEY_NETWORK_TYPE"
        fun create(
            token: Token.Active,
        ) = ReceiveTokenFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    override val statusBarColor: Int = R.color.backgroundButtonPrimary

    private val binding: FragmentReceiveTokenBinding by viewBinding()
    override val presenter: ReceiveTokenContract.Presenter by inject {
        parametersOf(token)
    }
    private val token: Token.Active by args(EXTRA_TOKEN)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLightStatusBar(isLight = false)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            toolbar.title = getString(R.string.receive_token_name, token.tokenName)
            val message = getString(R.string.receive_you_can_receive_token_message, token.tokenSymbol)
            val boldText = getString(R.string.receive_token_name_lower_case, token.tokenSymbol)
            titleTextView.text = SpanUtils.setTextBold(message, boldText)

            directAdressTopTextView.text = getString(R.string.receive_direct_token_address, token.tokenSymbol)
            mintAddressTopTextView.text = getString(R.string.receive_token_mint_address, token.tokenSymbol)

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
            receiveCardView.setOnShareQrClickListener { name, qrImage ->
                presenter.saveQr(name, qrImage, shareAfter = true)
            }
            receiveCardView.setQrWatermark(token.iconUrl)
            receiveCardView.showQrLoading(false)
            receiveCardView.setFaqVisibility(false)
            receiveCardView.setSelectNetworkVisibility(isVisible = token.isRenBTC)
        }
        setFragmentResultListener(REQUEST_KEY) { _, bundle ->
            val type = bundle.get(BUNDLE_KEY_NETWORK_TYPE) as NetworkType
            if (type == NetworkType.BITCOIN) {
                replaceFragment(ReceiveRenBtcFragment.create())
            }
        }
        presenter.loadData()
    }

    override fun onStop() {
        super.onStop()
        setLightStatusBar(isLight = true)
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        if (qrBitmap != null) {
            binding.receiveCardView.setQrBitmap(qrBitmap)
        }
    }

    override fun showReceiveToken(token: Token.Active) {
        with(binding) {
            mintAddressBottomTextView.text = token.mintAddress
            mintAddressView.setOnClickListener {
                requireContext().copyToClipBoard(token.mintAddress)
                toast(R.string.auth_copied)
            }
        }
    }

    override fun showUserData(userPublicKey: String, directPublicKey: String, username: Username?) {
        binding.directAddressBottomTextView.text = userPublicKey
        val username = username?.getFullUsername(requireContext())
        if (username != null) {
            binding.receiveCardView.setQrName(username)
        }
        binding.directAddressBottomTextView.text = directPublicKey
        binding.directTokenAddressView.setOnClickListener {
            requireContext().copyToClipBoard(directPublicKey)
            toast(R.string.auth_copied)
        }
        binding.receiveCardView.setQrValue(userPublicKey.highlightPublicKey(requireContext()))
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

    override fun showShareQr(qrImage: File, qrValue: String) {
        requireContext().shareScreenShot(qrImage, qrValue)
    }
}
