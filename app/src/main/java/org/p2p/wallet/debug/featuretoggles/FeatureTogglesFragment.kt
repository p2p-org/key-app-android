package org.p2p.wallet.debug.featuretoggles

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentDebugSettingsBinding
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.settings.ui.settings.SettingsAdapter
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class FeatureTogglesFragment :
    BaseMvpFragment<FeatureTogglesContract.View, FeatureTogglesContract.Presenter>(R.layout.fragment_feature_toggles),
    FeatureTogglesContract.View {

    companion object {
        fun create() = FeatureTogglesFragment()
    }

    override val presenter: FeatureTogglesContract.Presenter by inject()

    private val binding: FragmentDebugSettingsBinding by viewBinding()
    private val adapter = SettingsAdapter(
        onToggleCheckedListener = ::onToggleCheckedListener
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            settingsRecyclerView.attachAdapter(adapter)
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
        }

        presenter.loadData()
    }

    override fun showSettings(item: List<SettingsRow>) {
        adapter.setData(item)
    }

    private fun onToggleCheckedListener(@IdRes toggleId: Int, toggleChecked: Boolean) {
        presenter.onToggleCheckedListener(toggleId, toggleChecked)
    }
}
