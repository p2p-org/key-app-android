package org.p2p.wallet.receive.solana

import androidx.core.view.isVisible
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.glide.GlideManager
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentNewReceiveSolanaBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_TOKEN_LOGO_URL = "ARG_TOKEN_LOGO_URL"
private const val ARG_TOKEN_SYMBOL = "ARG_TOKEN_SYMBOL"

class NewReceiveSolanaFragment :
    BaseMvpFragment<NewReceiveSolanaContract.View, NewReceiveSolanaContract.Presenter>(
        R.layout.fragment_new_receive_solana
    ),
    NewReceiveSolanaContract.View {

    companion object {
        fun create(logoUrl: String, tokenSymbol: String) = NewReceiveSolanaFragment()
            .withArgs(
                ARG_TOKEN_LOGO_URL to logoUrl,
                ARG_TOKEN_SYMBOL to tokenSymbol
            )
    }

    override val presenter: NewReceiveSolanaContract.Presenter by inject()
    private val binding: FragmentNewReceiveSolanaBinding by viewBinding()
    private val glideManager: GlideManager by inject()
    private val logoUrl: String by args(ARG_TOKEN_LOGO_URL)
    private val tokenSymbol: String by args(ARG_TOKEN_SYMBOL)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.title = getString(R.string.receive_on_solana, tokenSymbol)
            toolbar.setNavigationOnClickListener { popBackStack() }
            glideManager.load(imageViewWatermark, logoUrl)

            layoutUsername.setOnClickListener {
                val username = binding.textViewUsername.text.toString()
                if (username.isEmpty()) return@setOnClickListener
                requireContext().copyToClipBoard(username)
                showUiKitSnackBar(messageResId = R.string.receive_username_copied)
            }
        }
        presenter.load()
    }

    override fun initView(qrBitmap: Bitmap, username: String?, tokenAddress: String) {
        with(binding) {
            buttonAction.setOnClickListener {
                requireContext().copyToClipBoard(tokenAddress)
                showUiKitSnackBar(messageResId = R.string.receive_sol_address_copied)
            }
            containerAddress.setOnClickListener {
                requireContext().copyToClipBoard(tokenAddress)
                showUiKitSnackBar(messageResId = R.string.receive_sol_address_copied)
            }
            textViewAddress.text = tokenAddress
            binding.imageViewQr.setImageBitmap(qrBitmap)
            showUsername(username)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    private fun showUsername(username: String?) {
        if (username == null) {
            binding.containerAddress.setBackgroundResource(R.drawable.bg_snow_rounded_16)
            binding.separator.isVisible = false
            binding.layoutUsername.isVisible = false
        } else {
            binding.textViewUsername.text = username
        }
    }
}
