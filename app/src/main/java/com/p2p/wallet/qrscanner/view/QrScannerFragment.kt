package com.p2p.wallet.qrscanner.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseFragment
import com.p2p.wallet.databinding.FragmentQrScannerBinding
import com.p2p.wallet.dashboard.ui.dialog.sendcoins.view.SendCoinsBottomSheet
import com.p2p.wallet.dashboard.ui.dialog.sendcoins.view.SendCoinsBottomSheet.Companion.TAG_SEND_COIN
import com.p2p.wallet.dashboard.ui.dialog.sendcoins.viewmodel.WalletAddressViewModel
import com.p2p.wallet.qrscanner.viewmodel.QrScannerViewModel
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import com.p2p.wallet.utils.withArgs
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class QrScannerFragment :
    BaseFragment(R.layout.fragment_qr_scanner),
    ZBarScannerView.ResultHandler {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 102
        const val GO_BACK_TO_SEND_COIN = "go_back_to_send_coin"

        fun create(goBack: Boolean) = QrScannerFragment().withArgs(
            GO_BACK_TO_SEND_COIN to goBack
        )
    }

    private val viewModel: QrScannerViewModel by viewModel()
    private val walletAddressViewModel: WalletAddressViewModel by sharedViewModel()
    private val binding: FragmentQrScannerBinding by viewBinding()

    private var scannerView: ZBarScannerView? = null
    private var isFromSendCoinsBottomSheet = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            vClose.setOnClickListener {
                walletAddressViewModel.postEnteredAmount()
                popBackStack()
            }
        }
        initializeQRCamera()
        checkForPermission()
        initData()
        observeData()
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
        scannerView = ZBarScannerView(context).apply {
            setResultHandler(this@QrScannerFragment)
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorTranslucent))
            setBorderColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            setLaserEnabled(false)
            setBorderStrokeWidth(resources.getDimensionPixelSize(R.dimen.dp_6))
            setBorderLineLength(resources.getDimensionPixelSize(R.dimen.dp_55))
            setBorderCornerRadius(resources.getDimensionPixelSize(R.dimen.dp_10))
            setSquareViewFinder(true)
            setupScanner()
            setAutoFocus(true)
        }
        startQRCamera()
        binding.containerScanner.addView(scannerView)
    }

    private fun startQRCamera() {
        scannerView?.startCamera()
    }

    private fun initData() {
        arguments?.let {
            isFromSendCoinsBottomSheet = it.getBoolean(GO_BACK_TO_SEND_COIN, false)
        }
    }

    private fun observeData() {
        viewModel.isCurrentAccountError.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            popBackStack()
        }
        viewModel.isCurrentAccount.observe(viewLifecycleOwner) {
            // if (it.walletAddress.isNotEmpty())
            if (isFromSendCoinsBottomSheet) {
                walletAddressViewModel.setWalletData(it)
                popBackStack()
            } else {
                SendCoinsBottomSheet.newInstance(
                    null,
                    it.walletAddress
                ).show(
                    parentFragmentManager,
                    TAG_SEND_COIN
                )
                popBackStack()
                walletAddressViewModel.setWalletData(it)
            }
        }
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
                popBackStack()
            }
            return
        }
    }
}