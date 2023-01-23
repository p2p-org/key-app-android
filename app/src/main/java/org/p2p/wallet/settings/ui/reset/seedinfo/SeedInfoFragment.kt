package org.p2p.wallet.settings.ui.reset.seedinfo

import android.os.Bundle

import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentResetSeedInfoBinding
import org.p2p.wallet.root.SystemIconsStyle
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding

class SeedInfoFragment : BaseFragment(R.layout.fragment_reset_seed_info) {

    companion object {
        fun create() = SeedInfoFragment()
    }

    private val binding: FragmentResetSeedInfoBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    override val customStatusBarStyle = SystemIconsStyle.WHITE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.SEED_INFO)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }
    }
}
