package org.p2p.wallet.main.ui.receive.solana

import android.graphics.Bitmap
import android.os.Bundle
import android.transition.TransitionManager.beginDelayedTransition
import android.view.View
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentReceiveSolanaBinding
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.colorFromTheme
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.shareText
import org.p2p.wallet.utils.showUrlInCustomTabs
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.auth.model.Username

class ReceiveSolanaFragment :
    BaseMvpFragment<ReceiveSolanaContract.View, ReceiveSolanaContract.Presenter>(R.layout.fragment_receive_solana),
    ReceiveSolanaContract.View {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"

        fun create(token: Token?) = ReceiveSolanaFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    private val token: Token? by args(EXTRA_TOKEN)

    override val presenter: ReceiveSolanaContract.Presenter by inject {
        parametersOf(token)
    }

    private val binding: FragmentReceiveSolanaBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            currenciesInfoTextView.clipToOutline = true
            currenciesInfoTextView.setOnClickListener {
                ReceiveSolanaInfoBottomSheetDialog.show(childFragmentManager)
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
        }

        presenter.loadData()
    }

    override fun showUserData(solToken: Token.Active, username: Username?) {
        with(binding) {
            fullAddressTextView.text = solToken.publicKey.cutMiddle()
            fullAddressTextView.setOnClickListener {
                requireContext().copyToClipBoard(solToken.publicKey)
                fullAddressTextView.setTextColor(colorFromTheme(R.attr.colorAccentPrimary))
                Toast.makeText(requireContext(), R.string.main_receive_address_copied, Toast.LENGTH_SHORT).show()
            }
            shareImageView.setOnClickListener {
                requireContext().shareText(solToken.publicKey)
            }

            if (username == null) return
            usernameTextView.isVisible = true
            usernameShareImageView.isVisible = true

            val fullUsername = username.getFullUsername(requireContext())
            usernameTextView.text = fullUsername
            usernameTextView.setOnClickListener {
                requireContext().copyToClipBoard(fullUsername)
                usernameTextView.setTextColor(colorFromTheme(R.attr.colorAccentPrimary))
                Toast.makeText(requireContext(), R.string.receive_username_copied, Toast.LENGTH_SHORT).show()
            }
            usernameShareImageView.setOnClickListener {
                requireContext().shareText(fullUsername)
            }
        }
    }

    override fun showReceiveToken(token: Token.Active) {
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