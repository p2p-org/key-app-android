package com.p2p.wallet.main.ui.receive

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
import com.bumptech.glide.Glide
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentReceiveBinding
import com.p2p.wallet.main.ui.select.SelectTokenFragment
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.utils.addFragment
import com.p2p.wallet.utils.args
import com.p2p.wallet.utils.copyToClipBoard
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.shareText
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

            copyImageView.setOnClickListener {
                requireContext().copyToClipBoard(mintTextView.text.toString())
            }

        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            SelectTokenFragment.REQUEST_KEY,
            viewLifecycleOwner,
            FragmentResultListener { _, result ->
                if (!result.containsKey(SelectTokenFragment.EXTRA_TOKEN)) return@FragmentResultListener
                val token = result.getParcelable<Token>(SelectTokenFragment.EXTRA_TOKEN)
                if (token != null) presenter.setReceiveToken(requireContext(), token)
            }
        )

        presenter.loadData(requireContext())
    }

    override fun showReceiveToken(token: Token) {
        with(binding) {
            Glide.with(tokenImageView).load(token.iconUrl).into(tokenImageView)

            tokenTextView.text = token.tokenSymbol
            addressTextView.text = token.getFormattedAddress()

            qrTitleTextView.text = getString(R.string.main_receive_public_address, token.tokenSymbol)
            fullAddressTextView.text = token.publicKey

            mintTitleTextView.text = getString(R.string.main_receive_mint_address, token.tokenSymbol)
            mintTextView.text = token.mintAddress

            tokenView.setOnClickListener {
                presenter.loadTokensForSelection()
            }
        }
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        with(binding) {
            qrImageView.setImageBitmap(qrBitmap)
        }
    }

    override fun navigateToTokenSelection(tokens: List<Token>) {
        addFragment(
            target = SelectTokenFragment.create(tokens),
            enter = R.anim.slide_up,
            exit = 0,
            popExit = R.anim.slide_down,
            popEnter = 0
        )
    }

    override fun showQrLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.qrImageView.isInvisible = isLoading
    }

    override fun showFullScreenLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }
}