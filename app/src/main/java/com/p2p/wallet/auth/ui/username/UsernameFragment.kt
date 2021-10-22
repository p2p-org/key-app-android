package com.p2p.wallet.auth.ui.username

import android.graphics.Bitmap
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.utils.edgetoedge.Edge
import com.p2p.wallet.utils.edgetoedge.edgeToEdge
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject

import com.p2p.wallet.databinding.FragmentUsernameBinding
import com.p2p.wallet.utils.copyToClipBoard
import com.p2p.wallet.utils.shareText

class UsernameFragment :
    BaseMvpFragment<UsernameContract.View,
        UsernameContract.Presenter>(R.layout.fragment_username),
    UsernameContract.View {

    companion object {
        fun create() = UsernameFragment()
    }

    override val presenter: UsernameContract.Presenter by inject()

    private val binding: FragmentUsernameBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }

            copyTextView.setOnClickListener {
                requireActivity().copyToClipBoard(addressTextView.text.toString())
            }

            shareTextView.setOnClickListener {
                requireActivity().shareText(addressTextView.text.toString())
            }

            saveTextView.setOnClickListener {
            }
        }

        presenter.loadData()
    }

    override fun showName(name: String?) {
        binding.nameTextView.text = name
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        binding.qrImageView.setImageBitmap(qrBitmap)
    }

    override fun showAddress(address: String?) {
        binding.addressTextView.text =
            address?.let { buildPartTextColor(it, ContextCompat.getColor(requireContext(), R.color.accentPrimary)) }
    }

    private fun buildPartTextColor(text: String, color: Int): SpannableString {
        val span = SpannableString(text)
        span.setSpan(ForegroundColorSpan(color), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(ForegroundColorSpan(color), text.length - 4, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return span
    }
}