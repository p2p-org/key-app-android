package org.p2p.wallet.debug.featuretoggles

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.inapp.DebugTogglesFeatureFlag
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
            buttonEnableLocal.setOnClickListener {
                presenter.switchDebugRemoteConfig(isDebugEnabled = true)
            }
            buttonEnableRemote.setOnClickListener {
                presenter.switchDebugRemoteConfig(isDebugEnabled = false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.load()
    }

    override fun showFeatureToggles(
        debugToggle: DebugTogglesFeatureFlag, toggleRows: List<FeatureToggleRow>
    ) {
        binding.buttonEnableLocal.isEnabled = !debugToggle.featureValue
        binding.buttonEnableRemote.isEnabled = debugToggle.featureValue

        if (debugToggle.featureValue) {
            binding.buttonEnableLocal.text = "Using local"
            binding.buttonEnableRemote.text = "Switch to remote"
        } else {
            binding.buttonEnableLocal.text = "Switch to local"
            binding.buttonEnableRemote.text = "Using remote"
        }

        adapter.setToggleRows(toggleRows)
    }
}
