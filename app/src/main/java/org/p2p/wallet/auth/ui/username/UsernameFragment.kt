package org.p2p.wallet.auth.ui.username

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.analytics.AnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentUsernameBinding
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.list.TokenListFragment
import org.p2p.wallet.utils.SpanUtils.highlightPublicKey
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.shareText
import org.p2p.wallet.utils.toast
import org.p2p.wallet.utils.viewbinding.viewBinding

class UsernameFragment :
    BaseMvpFragment<UsernameContract.View, UsernameContract.Presenter>(R.layout.fragment_username),
    UsernameContract.View {

    companion object {
        fun create() = UsernameFragment()
    }

    override val presenter: UsernameContract.Presenter by inject()

    private val binding: FragmentUsernameBinding by viewBinding()
    private val receiveAnalytics: ReceiveAnalytics by inject()
    private val analyticsInteractor: AnalyticsInteractor by inject()
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            presenter.saveQr(binding.nameTextView.text.toString())
        } else {
            toast(getString(R.string.auth_function_not_available))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            edgeToEdge {
                toolbar.fit { Edge.TopArc }
                bottomSheetView.fitMargin { Edge.BottomArc }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }

            saveButton.setOnClickListener {
                checkPermission()
                receiveAnalytics.logReceiveQrSaved(analyticsInteractor.getPreviousScreenName())
            }
            progressButton.setOnClickListener {
                replaceFragment(TokenListFragment.create())
            }
        }
        presenter.loadData()
    }

    override fun showUsername(username: Username) {
        val fullUsername = username.getFullUsername(requireContext())
        binding.nameTextView.text = fullUsername

        binding.copyButton.setOnClickListener {
            requireContext().copyToClipBoard(fullUsername)
            toast(R.string.common_copied)
            receiveAnalytics.logReceiveAddressCopied(analyticsInteractor.getPreviousScreenName())
        }

        binding.shareButton.setOnClickListener {
            requireContext().shareText(fullUsername)
            receiveAnalytics.logUserCardShared(analyticsInteractor.getPreviousScreenName())
        }
    }

    override fun renderQr(qrBitmap: Bitmap) {
        binding.qrImageView.setImageBitmap(qrBitmap)
    }

    override fun showAddress(address: String) {
        binding.addressTextView.text = address.highlightPublicKey(requireContext())
    }

    override fun showToastMessage(messageRes: Int) {
        toast(messageRes)
    }

    private fun checkPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ->
                presenter.saveQr(binding.nameTextView.text.toString())
            else -> requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}