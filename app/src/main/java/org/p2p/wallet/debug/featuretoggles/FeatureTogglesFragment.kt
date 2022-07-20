package org.p2p.wallet.debug.featuretoggles

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentFeatureTogglesBinding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class FeatureTogglesFragment :
    BaseMvpFragment<FeatureTogglesContract.View, FeatureTogglesContract.Presenter>(R.layout.fragment_feature_toggles),
    FeatureTogglesContract.View {

    companion object {
        fun create(): FeatureTogglesFragment = FeatureTogglesFragment()
    }

    override val presenter: FeatureTogglesContract.Presenter by inject()

    private val binding: FragmentFeatureTogglesBinding by viewBinding()
    private val adapter = FeatureTogglesAdapter(presenter::onToggleChanged)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            featureTogglesRecyclerView.attachAdapter(adapter)
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.load()
    }

    override fun showFeatureToggles(toggleRows: List<FeatureToggleRow>) {
        adapter.setToggleRows(toggleRows)
    }
}
