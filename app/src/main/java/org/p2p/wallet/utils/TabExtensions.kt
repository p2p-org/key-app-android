package org.p2p.wallet.utils

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import org.p2p.wallet.R

fun Context.showUrlInCustomTabs(url: String) {
    val uri = Uri.parse(url)
    val context = this
    val customTabsIntent = CustomTabsIntent.Builder().apply {
        setShowTitle(true)
        val mainBackgroundColor = context.getColor(R.color.backgroundPrimary)
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(mainBackgroundColor)
            .setNavigationBarColor(mainBackgroundColor)
            .setNavigationBarDividerColor(mainBackgroundColor)
            .setSecondaryToolbarColor(context.getColor(R.color.backgroundButtonPrimary))
            .build()
        setDefaultColorSchemeParams(defaultColors)
        val backBitmap = BitmapUtils.fromVectorDrawable(context, R.drawable.ic_back)
        if (backBitmap != null) {
            setCloseButtonIcon(backBitmap)
        }
        setStartAnimations(context, R.anim.nav_enter, R.anim.nav_exit)
        setShareState(CustomTabsIntent.SHARE_STATE_OFF)
    }.build()
    customTabsIntent.launchUrl(this, uri)
}

fun Fragment.showUrlInCustomTabs(url: String) {
    requireContext().showUrlInCustomTabs(url)
}

fun AppCompatActivity.showUrlInCustomTabs(url: String) {
    val uri = Uri.parse(url)
    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()
    customTabsIntent.launchUrl(this, uri)
}
