package org.p2p.wallet.receive.widget

import android.Manifest
import android.os.Build
import androidx.annotation.LayoutRes
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.permissions.PermissionDeniedDialog
import org.p2p.wallet.common.permissions.PermissionState
import org.p2p.wallet.common.permissions.PermissionsDialog
import org.p2p.wallet.common.permissions.PermissionsUtil

abstract class BaseQrCodeFragment<V : MvpView, P : MvpPresenter<V>>(
    @LayoutRes layoutRes: Int
) : BaseMvpFragment<V, P>(layoutRes), PermissionsDialog.Callback {

    abstract val receiveCardView: ReceiveCardView

    override fun onPermissionsResult(state: Map<String, PermissionState>, payload: Any?) {
        onCameraPermissionResult(state[Manifest.permission.WRITE_EXTERNAL_STORAGE])
    }

    protected fun checkStatusAndRequestPermissionsIfNotGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return true
        }
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
                with(receiveCardView) {
                    when (val action = getQrCodeLastAction()) {
                        QrView.QrCodeAction.SHARE -> requestShare()
                        QrView.QrCodeAction.SAVE -> requestSave()
                        else -> {
                            timber.log.Timber.e("Unsupported QrCodeAction $action")
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
