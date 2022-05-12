package org.p2p.wallet.auth.ui.username

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.permissions.PermissionDeniedDialog
import org.p2p.wallet.common.permissions.PermissionState
import org.p2p.wallet.common.permissions.PermissionsDialog
import org.p2p.wallet.common.permissions.PermissionsUtil
import org.p2p.wallet.databinding.FragmentUsernameBinding
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.list.TokenListFragment
import org.p2p.wallet.receive.widget.QrView
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.SpanUtils.highlightPublicKey
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.shareScreenShot
import org.p2p.wallet.utils.toast
import org.p2p.wallet.utils.viewbinding.viewBinding
import timber.log.Timber
import java.io.File

class UsernameFragment :
    BaseMvpFragment<UsernameContract.View, UsernameContract.Presenter>(R.layout.fragment_username),
    UsernameContract.View,
    PermissionsDialog.Callback {

    companion object {
        fun create() = UsernameFragment()
    }

    override val presenter: UsernameContract.Presenter by inject()

    private val binding: FragmentUsernameBinding by viewBinding()
    private val receiveAnalytics: ReceiveAnalytics by inject()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            toolbar.setNavigationOnClickListener { popBackStack() }
            receiveCardView.setOnRequestPermissions {
                checkStatusAndRequestPermissionsIfNotGranted()
            }
            receiveCardView.setOnShareQrClickListener { qrValue, qrImage ->
                presenter.saveQr(qrValue, qrImage, shareAfter = true)
                receiveAnalytics.logUserCardShared(analyticsInteractor.getPreviousScreenName())
            }
            receiveCardView.setOnSaveQrClickListener { qrValue, qrImage ->
                presenter.saveQr(qrValue, qrImage)
                receiveAnalytics.logReceiveQrSaved(analyticsInteractor.getPreviousScreenName())
            }
            receiveCardView.setSelectNetworkVisibility(isVisible = false)
            receiveCardView.setFaqVisibility(isVisible = false)
            receiveCardView.setTokenSymbol(Constants.SOL_NAME)
            receiveCardView.hideWatermark()
            progressButton.setOnClickListener {
                replaceFragment(TokenListFragment.create())
            }
        }
        presenter.loadData()
    }

    override fun showUsername(username: Username) {
        val fullUsername = username.getFullUsername(requireContext())
        binding.receiveCardView.setQrName(fullUsername)

        binding.receiveCardView.setOnCopyQrClickListener {
            receiveAnalytics.logReceiveAddressCopied(analyticsInteractor.getPreviousScreenName())
        }

        binding.receiveCardView.setOnShareQrClickListener { qrValue, qrImage ->
            presenter.saveQr(qrValue, qrImage, shareAfter = true)
            receiveAnalytics.logUserCardShared(analyticsInteractor.getPreviousScreenName())
        }
    }

    override fun renderQr(qrBitmap: Bitmap) {
        binding.receiveCardView.setQrBitmap(qrBitmap)
        binding.receiveCardView.showQrLoading(false)
    }

    override fun showAddress(address: String) {
        binding.receiveCardView.setQrValue(address.highlightPublicKey(requireContext()))
    }

    override fun showToastMessage(messageRes: Int) {
        toast(messageRes)
    }

    override fun showShareQr(qrImage: File, qrValue: String) {
        requireContext().shareScreenShot(qrImage, qrValue)
    }

    override fun onPermissionsResult(state: Map<String, PermissionState>, payload: Any?) {
        onCameraPermissionResult(state[Manifest.permission.WRITE_EXTERNAL_STORAGE])
    }

    private fun checkStatusAndRequestPermissionsIfNotGranted(): Boolean {
        return PermissionsUtil.isGranted(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).also { isGranted ->
            if (!isGranted) {
                PermissionsDialog.requestPermissions(
                    this,
                    listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                )
            }
        }
    }

    private fun onCameraPermissionResult(state: PermissionState?) {
        when (state) {
            PermissionState.GRANTED -> {
                with(binding.receiveCardView) {
                    when (val action = getQrCodeLastAction()) {
                        QrView.QrCodeAction.SHARE -> requestShare()
                        QrView.QrCodeAction.SAVE -> requestSave()
                        else -> {
                            Timber.e("Unsupported QrCodeAction $action")
                        }
                    }
                }
            }
            else -> PermissionDeniedDialog.show(
                fragment = this,
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
                title = getString(R.string.storage_permission_alert_title),
                message = getString(R.string.storage_permission_alert_message)
            )
        }
    }
}
