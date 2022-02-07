package org.p2p.wallet.settings.ui.settings

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.username.UsernameFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSettingsBinding
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class SettingsFragment :
    BaseMvpFragment<SettingsContract.View, SettingsContract.Presenter>(R.layout.fragment_settings),
    SettingsContract.View {

    companion object {
        fun create() = SettingsFragment()
    }

    override val presenter: SettingsContract.Presenter by inject()

    private val binding: FragmentSettingsBinding by viewBinding()
    private val adapter = SettingsAdapter(::onItemClickListener, ::onLogoutClickListener)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            recyclerView.attachAdapter(adapter)
        }

        presenter.loadData()
    }

    override fun showSettings(item: List<SettingsRow>) {
        adapter.setData(item)
    }

    private fun onItemClickListener(@StringRes titleResId: Int) {
        when (titleResId) {
            R.string.settings_username -> replaceFragment(UsernameFragment.create())
            R.string.settings_address_book -> TODO()
            R.string.settings_history -> TODO()
            R.string.settings_backup -> TODO()
            R.string.settings_wallet_pin -> TODO()
            R.string.settings_app_security -> TODO()
            R.string.settings_network -> TODO()
            R.string.settings_pay_fees_with -> TODO()
            R.string.settings_staying_up_in_date -> TODO()
            R.string.settings_default_currency -> TODO()
            R.string.settings_appearance -> TODO()
            R.string.settings_zero_balances -> TODO()
            R.string.settings_app_version -> TODO()

        }
    }

    private fun onLogoutClickListener() {
        presenter.logout()
    }
}