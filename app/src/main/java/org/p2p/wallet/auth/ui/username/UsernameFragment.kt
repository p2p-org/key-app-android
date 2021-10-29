package org.p2p.wallet.auth.ui.username

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject
import org.p2p.wallet.auth.model.Username

import org.p2p.wallet.databinding.FragmentUsernameBinding
import org.p2p.wallet.utils.colorFromTheme
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.shareText
import org.p2p.wallet.utils.toast

class UsernameFragment :
    BaseMvpFragment<UsernameContract.View,
        UsernameContract.Presenter>(R.layout.fragment_username),
    UsernameContract.View {

    companion object {
        fun create() = UsernameFragment()
    }

    override val presenter: UsernameContract.Presenter by inject()

    private val binding: FragmentUsernameBinding by viewBinding()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted)
                presenter.saveQr(binding.nameTextView.text.toString())
            else
                toast(getString(R.string.auth_function_not_available))
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }

            copyTextView.setOnClickListener {
                requireActivity().copyToClipBoard(nameTextView.text.toString())
                copySuccess()
            }

            shareTextView.setOnClickListener {
                requireActivity().shareText(nameTextView.text.toString())
            }

            saveTextView.setOnClickListener {
                checkPermission()
            }
        }

        presenter.loadData()
    }

    override fun showUsername(username: Username?) {
        binding.nameTextView.text = username?.getFullUsername(requireContext())
    }

    override fun renderQr(qrBitmap: Bitmap?) {
        binding.qrImageView.setImageBitmap(qrBitmap)
    }

    override fun showAddress(address: String?) {
        binding.addressTextView.text =
            address?.let { buildPartTextColor(it, colorFromTheme(R.attr.colorAccent)) }
    }

    override fun copySuccess() {
        toast(getString(R.string.auth_copied))
    }

    override fun saveSuccess() {
        toast(getString(R.string.auth_saved))
    }

    private fun buildPartTextColor(text: String, color: Int): SpannableString {
        val span = SpannableString(text)
        span.setSpan(ForegroundColorSpan(color), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(ForegroundColorSpan(color), text.length - 4, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return span
    }

    private fun checkPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ->
                presenter.saveQr(binding.nameTextView.text.toString())
            else -> requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}