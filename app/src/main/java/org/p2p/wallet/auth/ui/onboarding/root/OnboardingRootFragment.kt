package org.p2p.wallet.auth.ui.onboarding.root

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.onboarding.NewOnboardingFragment
import org.p2p.wallet.auth.ui.onboarding.continuestep.ContinueOnboardingFragment
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinFragment
import org.p2p.wallet.auth.ui.restore.common.CommonRestoreFragment
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.permissions.PermissionsUtil
import org.p2p.wallet.utils.popAndReplaceFragment
import org.p2p.wallet.utils.replaceFragment

class OnboardingRootFragment :
    BaseMvpFragment<OnboardingRootContract.View, OnboardingRootContract.Presenter>(R.layout.fragment_root_onboarding),
    OnboardingRootContract.View {

    companion object {
        fun create(): OnboardingRootFragment = OnboardingRootFragment()
    }

    override val presenter: OnboardingRootContract.Presenter by inject { parametersOf(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionName = Manifest.permission.POST_NOTIFICATIONS
            val isNotificationPermissionGranted = PermissionsUtil.isGranted(requireContext(), permissionName)
            presenter.logNotificationPermissionGranted(isNotificationPermissionGranted)
        }
    }

    override fun navigateToOnboarding() {
        navigateTo(NewOnboardingFragment.create())
    }

    override fun navigateToContinueOnboarding() {
        navigateTo(ContinueOnboardingFragment.create())
    }

    override fun navigateToRestore() {
        navigateTo(CommonRestoreFragment.createWithoutBack())
    }

    override fun navigateToCreatePin() {
        popAndReplaceFragment(NewCreatePinFragment.create(), inclusive = true)
    }

    override fun applyWindowInsets(rootView: View) {
        // do nothing
    }

    private fun navigateTo(fragment: Fragment) = replaceFragment(
        target = fragment,
        containerId = R.id.onboardingRootContainer,
        fragmentManager = childFragmentManager,
        addToBackStack = false
    )
}
