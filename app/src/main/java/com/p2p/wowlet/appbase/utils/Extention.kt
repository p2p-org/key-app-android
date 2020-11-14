package com.p2p.wowlet.appbase.utils

import android.app.Activity
import android.os.Build
import androidx.core.content.PermissionChecker
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.p2p.gp.appbase.utils.DataBindingDelegate

fun <Binding : ViewDataBinding> Fragment.dataBinding(layout: Int) =
    DataBindingDelegate<Binding>(this, layout)

inline fun <reified F> getCurrentFragment(navHostFragment: NavHostFragment): F? {
    return if (navHostFragment.isAdded && navHostFragment.childFragmentManager.fragments.size > 0) {
        if (navHostFragment.childFragmentManager.fragments[0] is F) {
            navHostFragment.childFragmentManager.fragments[0] as F
        } else {
            null
        }
    } else null
}

fun Activity.checkPermissionsGranted(permissions: ArrayList<String>): Boolean {
    permissions.forEach {
        if ((PermissionChecker.checkCallingOrSelfPermission(
                this,
                it
            ) != PermissionChecker.PERMISSION_GRANTED)
        )
            return false
    }
    return true
}

fun Activity.hasPermission(permission: String): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return PermissionChecker.checkSelfPermission(
            this,
            permission
        ) == PermissionChecker.PERMISSION_GRANTED
    }
    return true
}

