package org.p2p.wallet.qr.ui

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.mvp.NoOpPresenter
import org.p2p.wallet.common.permissions.PermissionDeniedDialog
import org.p2p.wallet.common.permissions.PermissionState
import org.p2p.wallet.common.permissions.PermissionsDialog
import org.p2p.wallet.common.permissions.PermissionsUtil
import org.p2p.wallet.databinding.FragmentScanQrBinding
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.utils.NoOp
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class ScanQrFragment :
    BaseMvpFragment<ScanQrContract.View, NoOpPresenter<ScanQrContract.View>>(R.layout.fragment_scan_qr),
    PermissionsDialog.Callback {

    companion object {
        fun create(successCallback: (String) -> Unit): ScanQrFragment =
            ScanQrFragment().apply { this.successCallback = successCallback }
    }

    override val statusBarColor: Int = R.color.bg_night
    override val navBarColor: Int = R.color.bg_night
    override val systemIconsStyle: SystemIconsStyle = SystemIconsStyle.WHITE

    private var successCallback: ((String) -> Unit)? = null
    private val binding: FragmentScanQrBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private var isPermissionsRequested = false
    private var isCameraStarted = false
    private val sendAnalytics: SendAnalytics by inject()

    override val presenter = NoOpPresenter<ScanQrContract.View>()

    private val qrDecodeStartTimeout by lazy {
        requireContext().resources
            .getInteger(android.R.integer.config_mediumAnimTime)
            .toLong()
    }

    private val barcodeCallback: ZXingScannerView.ResultHandler =
        ZXingScannerView.ResultHandler { rawResult ->
            rawResult?.text?.let {
                successCallback?.invoke(it)
                popBackStack()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }
        analyticsInteractor.logScreenOpenEvent(ScreenNames.Send.QR_CAMERA)

        with(binding) {
            barcodeView.setFormats(listOf(BarcodeFormat.QR_CODE))
            toolbar.setNavigationOnClickListener { onBackPressed() }
            imageViewFlash.setOnClickListener {
                barcodeView.toggleFlash()
                imageViewFlash.setImageResource(
                    if (barcodeView.flash) {
                        R.drawable.ic_flash_off
                    } else {
                        R.drawable.ic_flash_on
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isPermissionsRequested) {
            onCameraPermissionResult(PermissionsUtil.getPermissionStatus(this, Manifest.permission.CAMERA))
        } else {
            isPermissionsRequested = true
            PermissionsDialog.requestPermissions(this, listOf(Manifest.permission.CAMERA))
        }
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeView.stopCameraPreview()
    }

    override fun onStop() {
        super.onStop()
        isCameraStarted = false
        binding.barcodeView.stopCamera()
    }

    override fun onPermissionsResult(state: Map<String, PermissionState>, payload: Any?) {
        onCameraPermissionResult(state[Manifest.permission.CAMERA])
    }

    private fun onCameraPermissionResult(state: PermissionState?) {
        when (state) {
            PermissionState.GRANTED -> triggerQrScanner()
            PermissionState.DENIED -> showCameraNotAvailablePlaceholder {
                PermissionsDialog.requestPermissions(this, listOf(Manifest.permission.CAMERA))
            }
            PermissionState.PERMANENTLY_DENIED -> showCameraNotAvailablePlaceholder {
                PermissionDeniedDialog.show(
                    fragment = this,
                    permission = Manifest.permission.CAMERA,
                    title = getString(R.string.camera_permission_alert_title),
                    message = getString(R.string.camera_permission_alert_message)
                )
            }
            else -> NoOp
        }
    }

    private fun triggerQrScanner() {
        hideCameraNotAvailablePlaceholder()
        startCameraPreview()
    }

    private fun startCameraPreview() {
        with(binding) {
            if (!isCameraStarted) {
                isCameraStarted = true
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(qrDecodeStartTimeout)
                    barcodeView.startCamera()
                    barcodeView.resumeCameraPreview(barcodeCallback)
                }
            } else {
                barcodeView.resumeCameraPreview(barcodeCallback)
            }
            imageViewFlash.isVisible = true
        }
    }

    private inline fun showCameraNotAvailablePlaceholder(crossinline onRetryListener: () -> Unit) {
        with(binding) {
            layoutCameraPermission.isVisible = true
            buttonCameraPermissionRequest.isVisible = true
            buttonCameraPermissionRequest.setOnClickListener { onRetryListener() }
        }
    }

    private fun hideCameraNotAvailablePlaceholder() {
        with(binding) {
            layoutCameraPermission.isVisible = false
            buttonCameraPermissionRequest.isVisible = false
        }
    }

    private fun onBackPressed() {
        sendAnalytics.logSendQrGoingBack(
            qrCameraIsAvailable = isCameraStarted,
            qrGalleryIsAvailable = false,
            SendAnalytics.QrTab.CAMERA
        )
        popBackStack()
    }
}
