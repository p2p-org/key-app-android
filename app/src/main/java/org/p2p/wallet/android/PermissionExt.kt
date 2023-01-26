package org.p2p.wallet.android

import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import org.p2p.wallet.common.permissions.PermissionState
import org.p2p.wallet.common.permissions.PermissionsUtil

// TODO Replace all permission interaction in BaseQrFragment using this functions,
// skipped this in scope of this task
fun Fragment.requestPermissionNotification(callback: (state: PermissionState) -> Unit) {
    if (NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
        callback.invoke(PermissionState.GRANTED)
        return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionName = Manifest.permission.POST_NOTIFICATIONS

        getSinglePermissionBuilder { isGranted ->
            val resultState = proceedPermissionResult(permissionName, isGranted)
            callback.invoke(resultState)
        }
            .build(permissionName)
            .request()
    } else {
        callback.invoke(PermissionState.GRANTED)
    }
}

fun Fragment.requestPermission(permissionName: String, callback: (PermissionState) -> Unit) {
    if (isPermissionGranted(permissionName)) {
        callback.invoke(PermissionState.GRANTED)
    } else {
        getSinglePermissionBuilder { isGranted ->
            val resultState = proceedPermissionResult(permissionName, isGranted)
            callback.invoke(resultState)
        }
            .build(permissionName)
            .request()
    }
}

private fun Fragment.getPermissionState(permissionName: String): PermissionState {
    return PermissionsUtil.getPermissionStatus(this, permissionName)
}

private fun Fragment.isPermissionGranted(permissionName: String): Boolean {
    return PermissionsUtil.isGranted(requireContext(), permissionName)
}

private fun Fragment.getMultiplePermissionBuilder(
    callback: (Map<String, Boolean>) -> Unit
): MultiplePermissionRequest.Builder {
    val resultCallback = MultiplePermissionRequest.ResultCallback(callback)

    val resultListener = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        resultCallback
    )

    return MultiplePermissionRequest.Builder()
        .setResultLauncher(resultListener)
}

private fun Fragment.getSinglePermissionBuilder(
    callback: (Boolean) -> Unit
): SinglePermissionRequest.Builder {
    val resultCallback = SinglePermissionRequest.ResultCallback(callback)

    val resultListener = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        resultCallback
    )
    return SinglePermissionRequest.Builder()
        .setResultLauncher(resultListener)
}

private fun Fragment.proceedPermissionResult(permission: String, isGranted: Boolean): PermissionState {
    return when {
        isGranted -> PermissionState.GRANTED
        shouldShowRequestPermissionRationale(permission) -> PermissionState.DENIED
        else -> PermissionState.PERMANENTLY_DENIED
    }
}
