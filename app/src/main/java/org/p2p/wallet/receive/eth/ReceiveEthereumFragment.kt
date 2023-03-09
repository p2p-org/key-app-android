package org.p2p.wallet.receive.eth

import androidx.core.view.isVisible
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.glide.GlideManager
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentEthereumReceiveBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_TOKEN_LOGO_URL = "ARG_TOKEN_LOGO_URL"
private const val ARG_TOKEN_SYMBOL = "ARG_TOKEN_SYMBOL"

class EthereumReceiveFragment :
    BaseMvpFragment<ReceiveEthereumContract.View, ReceiveEthereumContract.Presenter>(
        R.layout.fragment_ethereum_receive
    ),
    ReceiveEthereumContract.View {

    companion object {
        fun create(tokenSymbol: String, tokenLogo: String): EthereumReceiveFragment =
            EthereumReceiveFragment().withArgs(
                ARG_TOKEN_SYMBOL to tokenSymbol,
                ARG_TOKEN_LOGO_URL to tokenLogo
            )
    }

    override val presenter: ReceiveEthereumContract.Presenter by inject()

    private val binding: FragmentEthereumReceiveBinding by viewBinding()
    private val glideManager: GlideManager by inject()
    private val logoUrl: String by args(ARG_TOKEN_LOGO_URL)
    private val tokenSymbol: String by args(ARG_TOKEN_SYMBOL)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.title = getString(R.string.receive_on_ethereum, tokenSymbol)
            toolbar.setNavigationOnClickListener { popBackStack() }
            glideManager.load(imageViewWatermark, logoUrl)
        }
        presenter.load()
    }

    override fun showQrAndAddress(qrBitmap: Bitmap, addressInHexString: String) {
        with(binding) {
            buttonAction.setOnClickListener {
                requireContext().copyToClipBoard(addressInHexString)
                showUiKitSnackBar(messageResId = R.string.receive_eth_address_copied)
            }
            containerAddress.setOnClickListener {
                requireContext().copyToClipBoard(addressInHexString)
                showUiKitSnackBar(messageResId = R.string.receive_eth_address_copied)
            }
            textViewAddress.text = addressInHexString
            binding.imageViewQr.setImageBitmap(qrBitmap)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }
}
