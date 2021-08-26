package com.p2p.wallet.main.ui.receive

import android.graphics.Bitmap
import android.os.Bundle
import android.transition.TransitionManager.beginDelayedTransition
import android.view.View
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentReceiveBinding
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.utils.args
import com.p2p.wallet.utils.colorFromTheme
import com.p2p.wallet.utils.copyToClipBoard
import com.p2p.wallet.utils.cutMiddle
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

            currenciesInfoTextView.clipToOutline = true
            currenciesInfoTextView.setOnClickListener {
                ReceiveInfoBottomSheetDialog.show(childFragmentManager)
            }

            detailsButton.setOnClickListener {
                detailsGroup.isVisible = !detailsGroup.isVisible

                val isVisible = detailsGroup.isVisible
                val resId = if (isVisible) {
                    R.string.main_receive_hide_details
                } else {
                    R.string.main_receive_show_details
                }

                detailsButton.setText(resId)
                beginDelayedTransition(root)
            }

            val isDetailed = token != null
            viewButton.isVisible = !isDetailed
            detailsButton.isVisible = isDetailed

            tabsRadioGroup.check(R.id.solanaButton)
        }

        presenter.loadData()
    }

    override fun showSolAddress(token: Token) {
        with(binding) {
            fullAddressTextView.text = token.publicKey.cutMiddle()
            fullAddressTextView.setOnClickListener {
                requireContext().copyToClipBoard(token.publicKey)
                fullAddressTextView.setTextColor(colorFromTheme(R.attr.colorAccentPrimary))
                Toast.makeText(requireContext(), R.string.main_receive_address_copied, Toast.LENGTH_SHORT).show()
            }
            shareImageView.setOnClickListener {
                requireContext().shareText(token.publicKey)
            }
        }
    }

    override fun showReceiveToken(token: Token) {
        with(binding) {
            viewButton.setOnClickListener {
                val url = getString(R.string.solanaWalletExplorer, token.publicKey)
                showUrlInCustomTabs(url)
            }

            shareAddressImageView.setOnClickListener {
                val url = getString(R.string.solanaWalletExplorer, token.publicKey)
                showUrlInCustomTabs(url)
            }

            shareMintAddressImageView.setOnClickListener {
                val url = getString(R.string.solanaWalletExplorer, token.mintAddress)
                showUrlInCustomTabs(url)
            }

            addressTitleTextView.text = getString(R.string.main_receive_address_format, token.tokenSymbol)
            addressTextView.text = token.publicKey

            mintAddressTitleTextView.text = getString(R.string.main_receive_mint_format, token.tokenSymbol)
            mintAddressTextView.text = token.mintAddress
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