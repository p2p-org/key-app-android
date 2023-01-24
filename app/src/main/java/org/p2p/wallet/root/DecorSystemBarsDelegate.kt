package org.p2p.wallet.root

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import android.graphics.Color
import android.os.Build

enum class SystemIconsStyle {
    BLACK, WHITE
}

class DecorSystemBarsDelegate constructor(
    private val activity: AppCompatActivity
) : DefaultLifecycleObserver {

    private var navigationBarStyle: SystemIconsStyle = SystemIconsStyle.BLACK
    private var statusBarStyle: SystemIconsStyle = SystemIconsStyle.BLACK

    private val windowInsetsController: WindowInsetsControllerCompat
        get() = WindowCompat.getInsetsController(activity.window, activity.window.decorView)

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        setupWindowInsets()
        updateSystemBarsStyle(statusBarStyle, navigationBarStyle)
    }

    fun updateSystemBarsStyle(
        statusBarStyle: SystemIconsStyle? = null,
        navigationBarStyle: SystemIconsStyle? = null,
    ) {
        statusBarStyle?.let { setStatusBarStyle(it) }
        navigationBarStyle?.let { setNavigationBarStyle(it) }
    }

    fun setStatusBarStyle(style: SystemIconsStyle) {
        windowInsetsController.isAppearanceLightStatusBars = when (style) {
            SystemIconsStyle.BLACK -> true
            SystemIconsStyle.WHITE -> false
        }
    }

    fun setNavigationBarStyle(style: SystemIconsStyle) {
        windowInsetsController.isAppearanceLightNavigationBars = when (style) {
            SystemIconsStyle.BLACK -> true
            SystemIconsStyle.WHITE -> false
        }
    }

    private fun setupWindowInsets() = with(activity) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }
}
