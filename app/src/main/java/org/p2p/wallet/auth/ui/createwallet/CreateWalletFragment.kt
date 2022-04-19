package org.p2p.wallet.auth.ui.createwallet

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.security.SecurityKeyFragment
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentCreateWalletBinding
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class CreateWalletFragment : BaseFragment(R.layout.fragment_create_wallet) {

    companion object {
        fun create() = CreateWalletFragment()
    }

    private val binding: FragmentCreateWalletBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        analyticsInteractor.logScreenOpenEvent(ScreenNames.OnBoarding.WALLET_CREATE)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            progressButton.setOnClickListener {
                replaceFragment(SecurityKeyFragment.create())
            }
        }
    }
}
