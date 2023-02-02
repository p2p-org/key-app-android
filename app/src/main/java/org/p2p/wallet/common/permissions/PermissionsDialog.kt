package org.p2p.wallet.common.permissions

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.withArgs

private val TAG = PermissionsDialog::class.java.simpleName
private const val PERMISSION_REQUEST_CODE = 42

private const val PERMISSIONS_EXTRA = "permissions"
private const val PAYLOAD_EXTRA = "payload"

@Suppress("DEPRECATION")
class PermissionsDialog : DialogFragment() {

    companion object {

        fun requestPermissions(fragment: Fragment, permissions: List<String>, payload: Any? = null) {
            val allPermissionsAreGranted = permissions.all { PermissionsUtil.isGranted(fragment.requireContext(), it) }
            if (allPermissionsAreGranted) {
                val status = permissions.associateWith { PermissionState.GRANTED }
                (fragment as? Callback)?.onPermissionsResult(status, payload)
            } else {
                show(fragment.childFragmentManager, permissions, payload)
            }
        }

        fun requestPermissions(activity: FragmentActivity, permissions: List<String>, payload: Any? = null) {
            val allPermissionsAreGranted = permissions.all { PermissionsUtil.isGranted(activity, it) }
            if (allPermissionsAreGranted) {
                val status = permissions.associateWith { PermissionState.GRANTED }
                (activity as? Callback)?.onPermissionsResult(status, payload)
            } else {
                show(activity.supportFragmentManager, permissions, payload)
            }
        }

        private fun show(fragmentManager: FragmentManager, permissions: List<String>, payload: Any?) {
            if (fragmentManager.findFragmentByTag(TAG) == null) {
                PermissionsDialog()
                    .withArgs(
                        PERMISSIONS_EXTRA to permissions.toTypedArray(),
                        PAYLOAD_EXTRA to payload
                    )
                    .show(fragmentManager, TAG)
            }
        }
    }

    private val permissions by args<Array<String>>(PERMISSIONS_EXTRA, emptyArray())
    private val payload by args<Any?>(PAYLOAD_EXTRA)

    private var isRationaleCache = HashMap<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        for (permission in permissions) {
            isRationaleCache[permission] = shouldShowRequestPermissionRationale(permission)
        }
        requestPermissions(permissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && permissions.isNotEmpty()) {
            val result = mutableMapOf<String, PermissionState>()
            permissions.forEachIndexed { index, permission ->
                result[permission] = when {
                    // if user grants permission
                    grantResults[index] == PackageManager.PERMISSION_GRANTED -> PermissionState.GRANTED
                    // if user choose 'Deny', we can ask permission again
                    shouldShowRequestPermissionRationale(permission) -> PermissionState.DENIED
                    // if user choose 'Deny' and check 'Never ask again' just now,
                    // the status still 'DENIED' because we must not show PermissionDeniedDialog
                    isRationaleCache[permission] == true -> PermissionState.DENIED
                    // if user choose 'Deny' and check 'Never ask again' some time ago,
                    // we can show PermissionDeniedDialog
                    else -> PermissionState.PERMANENTLY_DENIED
                }
            }
            (parentFragment as? Callback)?.onPermissionsResult(result, payload)
            (context as? Callback)?.onPermissionsResult(result, payload)
            dismissAllowingStateLoss()
        }
    }

    interface Callback {
        fun onPermissionsResult(state: Map<String, PermissionState>, payload: Any?)
    }
}
