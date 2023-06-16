package org.p2p.wallet.striga.kyc.ui

import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.home.ui.container.MainContainerFragment
import org.p2p.wallet.striga.kyc.sdk.StrigaKycSdkFacade
import org.p2p.wallet.striga.kyc.sdk.StrigaSdkInitParams
import org.p2p.wallet.utils.popBackStackTo

class StrigaKycFragment :
    BaseMvpFragment<StrigaKycContract.View, StrigaKycContract.Presenter>(R.layout.fragment_striga_kyc),
    StrigaKycContract.View {

    companion object {
        fun create(): StrigaKycFragment = StrigaKycFragment()
    }

    override val presenter: StrigaKycContract.Presenter by inject()

    private val strigaKycSdkFacade = StrigaKycSdkFacade()

    override fun startKyc(initParams: StrigaSdkInitParams) {
        strigaKycSdkFacade.startKycFlow(requireActivity(), initParams)
    }

    override fun navigateBack() {
        popBackStackTo(MainContainerFragment::class)
    }
}
