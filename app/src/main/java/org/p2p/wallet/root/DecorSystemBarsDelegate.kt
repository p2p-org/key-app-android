package org.p2p.wallet.root

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.graphics.Color
import android.os.Build
import android.view.Window

enum class SystemIconsStyle {
    BLACK, WHITE
}

private fun SystemIconsStyle.isLight(): Boolean = when (this) {
    SystemIconsStyle.BLACK -> true
    SystemIconsStyle.WHITE -> false
}

class DecorSystemBarsDelegate constructor(
    private val window: Window,
) {

    constructor(activity: AppCompatActivity) : this(activity.window)

    private var defaultNavigationBarStyle: SystemIconsStyle = SystemIconsStyle.BLACK
    private var defaultStatusBarStyle: SystemIconsStyle = SystemIconsStyle.BLACK

    private val windowInsetsController: WindowInsetsControllerCompat
        get() = WindowCompat.getInsetsController(window, window.decorView)

    fun onCreate() {
        setupWindowInsets()
        updateSystemBarsStyle(defaultStatusBarStyle, defaultNavigationBarStyle)
    }

    fun updateSystemBarsStyle(
        statusBarStyle: SystemIconsStyle? = null,
        navigationBarStyle: SystemIconsStyle? = null,
    ) {
        setStatusBarStyle(statusBarStyle ?: defaultStatusBarStyle)
        setNavigationBarStyle(navigationBarStyle ?: defaultNavigationBarStyle)
    }

    private fun setStatusBarStyle(style: SystemIconsStyle) {
        val isLight = style.isLight()
        if (windowInsetsController.isAppearanceLightStatusBars != isLight) {
            windowInsetsController.isAppearanceLightStatusBars = isLight
        }
    }

    private fun setNavigationBarStyle(style: SystemIconsStyle) {
        val isLight = style.isLight()
        if (windowInsetsController.isAppearanceLightNavigationBars != isLight) {
            windowInsetsController.isAppearanceLightNavigationBars = isLight
        }
    }

    private fun setupWindowInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }
}
