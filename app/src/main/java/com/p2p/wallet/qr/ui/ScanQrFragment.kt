package com.p2p.wallet.qr.ui

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.p2p.wallet.R
import com.p2p.wallet.common.permissions.PermissionDeniedDialog
import com.p2p.wallet.common.permissions.PermissionState
import com.p2p.wallet.common.permissions.PermissionsDialog
import com.p2p.wallet.common.permissions.PermissionsUtil
import com.p2p.wallet.databinding.FragmentScanQrBinding
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScanQrFragment :
    Fragment(R.layout.fragment_scan_qr),
    PermissionsDialog.Callback {

    companion object {
        fun create(
            successCallback: (String) -> Unit
        ) = ScanQrFragment().apply {
            this.successCallback = successCallback
        }
    }

    private var successCallback: ((String) -> Unit)? = null

    private val binding: FragmentScanQrBinding by viewBinding()

    private var isPermissionsRequested = false
    private var isCameraStarted = false

    private val qrDecodeStartTimeout by lazy {
        requireContext().resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
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

        with(binding) {
            barcodeView.setFormats(listOf(BarcodeFormat.QR_CODE))
            closeImageView.setOnClickListener { popBackStack() }
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
            PermissionState.GRANTED -> {
                triggerQrScanner()
            }
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
        }
    }

    private inline fun showCameraNotAvailablePlaceholder(crossinline onRetryListener: () -> Unit) {
        with(binding) {
            cameraPermissionDeniedPlaceholder.isVisible = true
            cameraPermissionRequestButton.isVisible = true
            cameraPermissionRequestButton.setOnClickListener { onRetryListener() }
        }
    }

    private fun hideCameraNotAvailablePlaceholder() {
        with(binding) {
            cameraPermissionDeniedPlaceholder.isVisible = false
            cameraPermissionRequestButton.isVisible = false
        }
    }

    fun resumeCameraPreview() {
        binding.barcodeView.resumeCameraPreview(barcodeCallback)
    }
}