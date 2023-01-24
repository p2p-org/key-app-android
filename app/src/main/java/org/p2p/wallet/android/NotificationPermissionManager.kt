package org.p2p.wallet.android

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentManager
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.notification.AppNotificationManager

class NotificationPermissionManager(
    private val context: Context
) {

    private lateinit var activityResultLauncher: ActivityResultLauncher<String>
    private var isPermissionRequested: Boolean = false

    private val callback = PermissionResultCallback { isPermissionGranted ->
    }

    fun setup(resultLauncher: ActivityResultLauncher<String>) {
        this.activityResultLauncher = resultLauncher
    }

    fun setupFragmentStackListener(fragmentManager: FragmentManager) {
        fragmentManager.addOnBackStackChangedListener {
            val lastFragmentInStack = fragmentManager.fragments.lastOrNull()
            if (lastFragmentInStack is MainFragment && !isPermissionRequested) {
                checkForNotificationPermission()
            }
        }
    }

    fun getResultCallback(): PermissionResultCallback = callback

    private fun checkForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                return
            }
            activityResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
