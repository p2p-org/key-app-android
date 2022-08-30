package org.p2p.wallet.settings.ui.settings.adapter

import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem

fun interface SettingsItemClickListener {
    fun onSettingsItemClicked(clickedSettings: SettingsItem)
}
