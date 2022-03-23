package org.p2p.wallet.common.permissions

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import org.p2p.wallet.R
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.withArgs

private const val PERMISSION_EXTRA = "PERMISSION_EXTRA"
private const val DIALOG_MESSAGE_EXTRA = "DIALOG_MESSAGE_EXTRA"
private const val DIALOG_TITLE_EXTRA = "DIALOG_TITLE_EXTRA"

class PermissionDeniedDialog : DialogFragment() {

    companion object {

        fun show(fragment: Fragment, permission: String, title: String? = null, message: String? = null) {
            show(fragment.childFragmentManager, permission, title, message)
        }

        fun show(activity: FragmentActivity, permission: String, title: String? = null, message: String? = null) {
            show(activity.supportFragmentManager, permission, title, message)
        }

        private fun show(fragmentManager: FragmentManager, permission: String, title: String?, message: String?) {
            PermissionDeniedDialog()
                .withArgs(
                    PERMISSION_EXTRA to permission,
                    DIALOG_TITLE_EXTRA to title,
                    DIALOG_MESSAGE_EXTRA to message
                )
                .show(fragmentManager, PermissionDeniedDialog::class.java.simpleName)
        }

        fun openSettings(context: Context) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", context.packageName, null))
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        }
    }

    private val permission: String by args(PERMISSION_EXTRA)
    private val title: String? by args(DIALOG_TITLE_EXTRA)
    private val message: String? by args(DIALOG_MESSAGE_EXTRA)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), R.style.WalletTheme_AlertDialog)
            .setCancelable(true)
            .setTitle(title ?: getTitle(permission))
            .setMessage(message ?: getText(permission))
            .setPositiveButton(R.string.common_settings) { _, _ -> openSettings(requireContext()) }
            .setNegativeButton(R.string.common_cancel) { _, _ -> }
            .create()
    }

    private fun getTitle(permission: String) =
        when (permission) {
            Manifest.permission.CAMERA -> getString(R.string.camera_permission_alert_title)
            else -> throw IllegalStateException("Unknown permission type $permission")
        }

    private fun getText(permission: String) =
        when (permission) {
            Manifest.permission.CAMERA -> getString(R.string.camera_permission_alert_message)
            else -> throw IllegalStateException("Unknown permission type $permission")
        }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (parentFragment as? Callback)?.onPermissionDeniedDismiss(permission)
        (context as? Callback)?.onPermissionDeniedDismiss(permission)
    }

    interface Callback {
        fun onPermissionDeniedDismiss(permission: String)
    }
}
