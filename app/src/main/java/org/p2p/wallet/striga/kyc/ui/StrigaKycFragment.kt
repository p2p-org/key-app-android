package org.p2p.wallet.striga.kyc.ui

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.striga.kyc.sdk.StrigaKycSdkFacade
import org.p2p.wallet.striga.kyc.sdk.StrigaSdkInitParams
import org.p2p.wallet.utils.popBackStackTo

class StrigaKycFragment :
    BaseMvpFragment<StrigaKycContract.View, StrigaKycContract.Presenter>(R.layout.fragment_striga_kyc),
    StrigaKycContract.View {

    // navigateBack may be called too early, so we need to delay it until lifecycle is in resumed state
    // attempting to popBackStack before `RESUMED` state will result in crash or
    // freezing FragmentManager.popBackStackImmediate call
    private val backPressDeferredHandler = MutableSharedFlow<Unit>(replay = 1)

    companion object {
        fun create(): StrigaKycFragment = StrigaKycFragment()
    }

    override val presenter: StrigaKycContract.Presenter by inject()

    private val strigaKycSdkFacade = StrigaKycSdkFacade()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                backPressDeferredHandler.collect {
                    popBackStackTo(MainFragment::class, immediate = false)
                }
            }
        }
    }

    override fun startKyc(initParams: StrigaSdkInitParams) {
        strigaKycSdkFacade.startKycFlow(requireActivity(), initParams)
    }

    override fun navigateBack() {
        lifecycleScope.launch {
            backPressDeferredHandler.emit(Unit)
        }
    }
}
