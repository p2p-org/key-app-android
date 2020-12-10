package com.p2p.wowlet.fragment.qrscanner.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentQrScannerBinding
import com.p2p.wowlet.fragment.qrscanner.viewmodel.QrScannerViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView

class QrScannerFragment : FragmentBaseMVVM<QrScannerViewModel, FragmentQrScannerBinding>(),
    ZBarScannerView.ResultHandler {
    val isCheckAllowPermission = false
    override val viewModel: QrScannerViewModel by viewModel()
    override val binding: FragmentQrScannerBinding by dataBinding(R.layout.fragment_qr_scanner)

    private var scannerView: ZBarScannerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@QrScannerFragment.viewModel
        }
        activity?.let {
            (it as MainActivity).showHideNav(false)
        }
        initializeQRCamera()
        checkForPermission()
    }

    private fun checkForPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            initializeQRCamera()
        } else {
            requestPermission()
        }

    }

    private fun initializeQRCamera() {
        scannerView = ZBarScannerView(context)
        scannerView?.setResultHandler(this)
        scannerView?.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorTranslucent))
        scannerView?.setBorderColor(ContextCompat.getColor(context!!, android.R.color.white))
        scannerView?.setLaserEnabled(false)
        scannerView?.setBorderStrokeWidth(4)
        scannerView?.setBorderCornerRadius(24)
        scannerView?.setSquareViewFinder(true)
        scannerView?.setupScanner()
        scannerView?.setAutoFocus(true)
        startQRCamera()
        binding.containerScanner.addView(scannerView)
    }

    private fun startQRCamera() {
        scannerView?.startCamera()
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is NavigateUpViewCommand -> {
                activity?.let {
                    (it as MainActivity).showHideNav(true)
                }
                navigateFragment(command.destinationId)
            }
            is NavigateSendCoinViewCommand -> navigateFragment(
                command.destinationId,
                command.bundle
            )
        }
    }

    override fun observes() {
        observe(viewModel.isCurrentAccountError) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
        observe(viewModel.isCurrentAccount) {
            if (it.isWalletAddress) {
                viewModel.goToSendCoinFragment(it.walletKey)
            } else {
                Toast.makeText(context, "There is not wallet key", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun navigateUp() {
        viewModel.navigateUp()
    }

    override fun handleResult(rawResult: Result?) {
        rawResult?.let {
            viewModel.getAccountInfo(it.contents)
        }
    }

    override fun onPause() {
        super.onPause()
        if (scannerView != null)
            scannerView?.stopCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (scannerView != null)
            scannerView?.stopCamera()
    }


    private fun requestPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeQRCamera()
            } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                viewModel.navigateUp()
            }
            return

        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 102
    }
}