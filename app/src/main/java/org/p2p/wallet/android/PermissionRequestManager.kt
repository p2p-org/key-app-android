package org.p2p.wallet.android

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import org.p2p.wallet.common.permissions.PermissionsUtil

class PermissionRequestManager(
    private val context: Context
) {

    private lateinit var activityResultLauncher: ActivityResultLauncher<String>

    fun setupResultListener(resultListener: ActivityResultLauncher<String>) {
        activityResultLauncher = resultListener
    }

    fun requestPermissions(fragment: Fragment, permissions: List<String>) {
        val allPermissionsAreGranted = permissions.all { PermissionsUtil.isGranted(fragment.requireContext(), it) }
    }
}
