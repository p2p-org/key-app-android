package org.p2p.wallet.qr.ui

import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import android.Manifest
import android.content.DialogInterface
import android.content.DialogInterface.OnCancelListener
import android.os.Bundle
import android.view.View
import com.google.zxing.BarcodeFormat
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.koin.android.ext.android.inject
import org.p2p.solanaj.core.PublicKey
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
import org.p2p.wallet.root.SystemIconsStyle
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import timber.log.Timber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val EXTRA_KEY = "EXTRA_KEY"
private const val EXTRA_RESULT = "EXTRA_RESULT"

class ScanQrFragment :
    BaseMvpFragment<ScanQrContract.View, NoOpPresenter<ScanQrContract.View>>(R.layout.fragment_scan_qr),
    PermissionsDialog.Callback,
    OnCancelListener {

    companion object {
        fun create(
            requestKey: String,
            resultKey: String
        ): ScanQrFragment =
            ScanQrFragment().withArgs(
                EXTRA_KEY to requestKey,
                EXTRA_RESULT to resultKey
            )
    }

    override val customStatusBarStyle = SystemIconsStyle.WHITE
    override val customNavigationBarStyle = SystemIconsStyle.WHITE

    private val requestKey: String by args(EXTRA_KEY)
    private val resultKey: String by args(EXTRA_RESULT)

    private val binding: FragmentScanQrBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private var isPermissionsRequested = false
    private var isCameraStarted = false
    private val sendAnalytics: SendAnalytics by inject()

    override val presenter = NoOpPresenter<ScanQrContract.View>()

    private var errorDialog: AlertDialog? = null

    private val qrDecodeStartTimeout by lazy {
        requireContext().resources
            .getInteger(android.R.integer.config_mediumAnimTime)
            .toLong()
    }

    private val barcodeCallback = ZXingScannerView.ResultHandler { rawAddressResult ->
        rawAddressResult?.text?.let(::validateAddress) ?: showInvalidDataError()
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
                    if (barcodeView.flash) R.drawable.ic_flash_off else R.drawable.ic_flash_on
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
        errorDialog?.dismiss()
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
            PermissionState.PERMANENTLY_DENIED -> showCameraNotAvailablePlaceholder(onRetryListener = {
                PermissionDeniedDialog.show(
                    fragment = this,
                    permission = Manifest.permission.CAMERA,
                    title = getString(R.string.camera_permission_alert_title),
                    message = getString(R.string.camera_permission_alert_message)
                )
            })
            else -> Unit
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
            containerCameraPermission.isVisible = true
            buttonCameraPermissionRequest.isVisible = true
            buttonCameraPermissionRequest.setOnClickListener { onRetryListener() }
        }
    }

    private fun hideCameraNotAvailablePlaceholder() {
        with(binding) {
            containerCameraPermission.isVisible = false
            buttonCameraPermissionRequest.isVisible = false
        }
    }

    private fun validateAddress(address: String) {
        try {
            PublicKey(address)
            setFragmentResult(requestKey, bundleOf(resultKey to address))
            popBackStack()
            showUiKitSnackBar(
                message = getString(R.string.qr_address_found, address.cutMiddle(CUT_ADDRESS_SYMBOLS_COUNT)),
                actionButtonResId = android.R.string.ok,
                actionBlock = {}
            )
        } catch (e: Throwable) {
            Timber.i(e, "No address in this scanned data: $address")
            AlertDialog.Builder(requireContext())
                .setCancelable(true)
                .setMessage(R.string.qr_no_address)
                .setPositiveButton(android.R.string.ok) { _, _ -> startCameraPreview() }
                .setOnCancelListener(this)
                .show()
                .also { errorDialog = it }
        }
    }

    private fun showInvalidDataError() {
        AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setMessage(R.string.qr_common_error)
            .setPositiveButton(android.R.string.ok) { _, _ -> startCameraPreview() }
            .setOnCancelListener(this)
            .show()
            .also { errorDialog = it }
    }

    override fun onCancel(dialog: DialogInterface?) {
        startCameraPreview()
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
