package org.p2p.wallet.utils

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment

fun Context.showUrlInCustomTabs(url: String) {
    val uri = Uri.parse(url)
    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()
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
