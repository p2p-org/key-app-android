package org.p2p.wallet.android

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.result.ActivityResultCallback
import androidx.fragment.app.FragmentManager
import org.p2p.wallet.common.permissions.PermissionsUtil
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.notification.AppNotificationManager

class RequestPermissionBuilder(
    private val context: Context,
    private var isPermissionRequested: Boolean,
    private val contract: PermissionContract,
    private val callback: (String, Boolean) -> Unit
) {

    fun setupFragmentStackListener(fragmentManager: FragmentManager) {
        fragmentManager.addOnBackStackChangedListener {
            val lastFragmentInStack = fragmentManager.fragments.lastOrNull()
            if (lastFragmentInStack is MainFragment && !isPermissionRequested) {
                checkForNotificationPermission()
            }
        }
    }

    private fun checkForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (PermissionsUtil.isGranted(context, contract.name)) {
                return
            }
            contract.resultLauncher.launch(Manifest.permission.CAMERA)
        } else {
            AppNotificationManager.createNotificationChannels(context)
        }
    }

    inner class PermissionResultCallback(private val onPermissionStatusChanged: (Boolean) -> Unit) :
        ActivityResultCallback<Boolean> {

        override fun onActivityResult(isPermissionGranted: Boolean) {
            onPermissionStatusChanged.invoke(isPermissionGranted)
        }
    }
}
