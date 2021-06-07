package com.p2p.wallet.main.ui.receive

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentReceiveBinding
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.utils.args
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.shareText
import com.p2p.wallet.utils.showUrlInCustomTabs
import com.p2p.wallet.utils.viewbinding.viewBinding
import com.p2p.wallet.utils.withArgs
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class ReceiveFragment :
    BaseMvpFragment<ReceiveContract.View, ReceiveContract.Presenter>(R.layout.fragment_receive),
    ReceiveContract.View {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"

        fun create(token: Token?) = ReceiveFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    private val token: Token? by args(EXTRA_TOKEN)

    override val presenter: ReceiveContract.Presenter by inject {
        parametersOf(token)
    }

    private val binding: FragmentReceiveBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            toolbar.setOnMenuItemClickListener {
                if (it.itemId == R.id.itemShare) {
                    requireContext().shareText(fullAddressTextView.text.toString())
                    return@setOnMenuItemClickListener true
                }

                return@setOnMenuItemClickListener false
            }
        }

        presenter.loadData()
    }

    override fun showReceiveToken(token: Token) {
        with(binding) {

            qrTitleTextView.text = getString(R.string.main_receive_public_address, token.tokenSymbol)
            fullAddressTextView.text = token.publicKey

            viewButton.setOnClickListener {
                val url = getString(R.string.solanaWalletExplorer, token)
                showUrlInCustomTabs(url)
            }
        }
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        with(binding) {
            qrImageView.setImageBitmap(qrBitmap)
        }
    }

    override fun showQrLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.qrImageView.isInvisible = isLoading
    }

    override fun showFullScreenLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }
}