package org.p2p.wallet.settings.ui.reset.seedinfo

import android.os.Bundle

import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.AnalyticsInteractor
import org.p2p.wallet.common.analytics.EventsName
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentResetSeedInfoBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class SeedInfoFragment : BaseFragment(R.layout.fragment_reset_seed_info) {

    companion object {
        fun create() = SeedInfoFragment()
    }

    private val binding: FragmentResetSeedInfoBinding by viewBinding()
    private val analyticsInteractor: AnalyticsInteractor by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(EventsName.OnBoarding.SEED_INFO)
    }
}