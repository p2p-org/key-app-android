package com.p2p.wallet.settings.ui.appearance

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BaseMvpFragment
import com.p2p.wallet.databinding.FragmentAppearanceBinding
import com.p2p.wallet.settings.interactor.Theme
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.viewbinding.viewBinding
import org.koin.android.ext.android.inject

class AppearanceFragment :
    BaseMvpFragment<AppearanceContract.View, AppearanceContract.Presenter>(R.layout.fragment_appearance),
    AppearanceContract.View,
    RadioGroup.OnCheckedChangeListener {

    companion object {
        fun create() = AppearanceFragment()
    }

    override val presenter: AppearanceContract.Presenter by inject()

    private val binding: FragmentAppearanceBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }

            themeGroup.setOnCheckedChangeListener(this@AppearanceFragment)
        }

        presenter.loadThemeSettings()
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        val theme = when (checkedId) {
            R.id.lightButton -> Theme.Light
            R.id.darkButton -> Theme.Dark
            else -> Theme.System
        }
        presenter.setTheme(theme)
    }

    override fun showCurrentTheme(themeButtonId: Int) {
        with(binding) {
            themeGroup.setOnCheckedChangeListener(null)
            themeGroup.check(themeButtonId)
            themeGroup.setOnCheckedChangeListener(this@AppearanceFragment)
        }
    }
}