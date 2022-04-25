package org.p2p.wallet.settings.ui.appearance

import androidx.appcompat.app.AppCompatDelegate
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.settings.interactor.Theme
import org.p2p.wallet.settings.interactor.ThemeInteractor

class AppearancePresenter(
    private val themeInteractor: ThemeInteractor
) : BasePresenter<AppearanceContract.View>(), AppearanceContract.Presenter {

    override fun loadThemeSettings() {
        val themeButtonId = when (themeInteractor.getNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> R.id.darkButton
            AppCompatDelegate.MODE_NIGHT_NO -> R.id.lightButton
            else -> R.id.systemButton
        }

        view?.showCurrentTheme(themeButtonId)
    }

    override fun setTheme(theme: Theme) {
        themeInteractor.setTheme(theme)
    }
}
