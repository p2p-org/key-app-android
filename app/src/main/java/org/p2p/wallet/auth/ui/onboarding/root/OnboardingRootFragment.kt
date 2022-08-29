package org.p2p.wallet.auth.ui.onboarding.root

import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.continuestep.ContinueOnboardingFragment
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.utils.replaceFragment

class OnboardingRootFragment :
    BaseMvpFragment<OnboardingRootContract.View, OnboardingRootContract.Presenter>(R.layout.fragment_root_onboarding),
    OnboardingRootContract.View {

    companion object {
        fun create(): OnboardingRootFragment = OnboardingRootFragment()
    }

    override val presenter: OnboardingRootContract.Presenter by inject { parametersOf(this) }

    override val statusBarColor: Int = R.color.bg_lime
    override val navBarColor: Int = R.color.bg_night

    override fun navigateToOnboarding() {
        navigateTo(NewCreatePinFragment.create())
    }

    override fun navigateToContinueOnboarding() {
        navigateTo(ContinueOnboardingFragment.create())
    }

    override fun navigateToCreatePin() {
        navigateTo(NewCreatePinFragment.create())
    }

    private fun navigateTo(fragment: Fragment) = replaceFragment(
        target = fragment,
        containerId = R.id.onboardingRootContainer,
        fragmentManager = childFragmentManager,
        addToBackStack = false
    )
}
