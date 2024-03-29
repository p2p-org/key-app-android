package org.p2p.wallet.receive.eth

import androidx.core.view.isVisible
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import java.math.BigDecimal
import org.p2p.core.glide.GlideManager
import org.p2p.core.utils.asUsd
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentEthereumReceiveBinding
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.vibrate
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
        fun create(tokenSymbol: String, tokenLogoUrl: String): EthereumReceiveFragment =
            EthereumReceiveFragment().withArgs(
                ARG_TOKEN_SYMBOL to tokenSymbol,
                ARG_TOKEN_LOGO_URL to tokenLogoUrl
            )
    }

    override val presenter: ReceiveEthereumContract.Presenter by inject()

    private val binding: FragmentEthereumReceiveBinding by viewBinding()
    private val glideManager: GlideManager by inject()
    private val receiveAnalytics: ReceiveAnalytics by inject()

    private val logoUrl: String by args(ARG_TOKEN_LOGO_URL)
    private val tokenSymbol: String by args(ARG_TOKEN_SYMBOL)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.title = getString(R.string.receive_on_ethereum, tokenSymbol)
            toolbar.setNavigationOnClickListener { popBackStack() }
            textViewFirstNumValue.text = getString(R.string.receive_ethereum_step_1, tokenSymbol)
            glideManager.load(imageViewWatermark, logoUrl)
        }
        presenter.load()
    }

    override fun showQrAndAddress(qrBitmap: Bitmap, addressInHexString: String) {
        with(binding) {
            buttonAction.setOnClickListener {
                receiveAnalytics.logAddressCopyButtonClicked(ReceiveAnalytics.AnalyticsReceiveNetwork.ETHEREUM)
                requireContext().copyToClipBoard(addressInHexString)
                requireContext().vibrate()
                showUiKitSnackBar(messageResId = R.string.receive_eth_address_copied)
            }
            containerAddress.apply {
                textViewAddress.text = addressInHexString
                setOnClickListener {
                    receiveAnalytics.logAddressCopyLongClicked(ReceiveAnalytics.AnalyticsReceiveNetwork.ETHEREUM)
                    requireContext().copyToClipBoard(addressInHexString)
                    requireContext().vibrate()
                    showUiKitSnackBar(messageResId = R.string.receive_eth_address_copied)
                }
            }
            binding.imageViewQr.setImageBitmap(qrBitmap)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun setMinAmountForFreeFee(minAmountForFreeFee: BigDecimal) {
        binding.textViewBanner.text = getString(
            R.string.receive_ethereum_banner_text_format,
            minAmountForFreeFee.asUsd()
        )
    }
}
