package org.p2p.wallet.settings.ui.settings

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSettingsBinding
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.utils.attachAdapter
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
        }
    }

    private fun onLogoutClickListener() {
    }
}