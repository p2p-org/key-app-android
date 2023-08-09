package org.p2p.wallet.home.onofframp

import androidx.fragment.app.Fragment
import org.p2p.wallet.common.NavigationDestination
import org.p2p.wallet.common.NavigationStrategy
import org.p2p.wallet.home.addmoney.ui.AddMoneyDialog
import org.p2p.wallet.home.onofframp.ui.OnOffRampCountrySelectionFragment
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.striga.StrigaFragmentFactory
import org.p2p.wallet.striga.offramp.ui.StrigaOffRampFragment
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.replaceFragmentForResult

class OnOffRampNavigator(
    private val settingsInteractor: SettingsInteractor,
    private val strigaFragmentFactory: StrigaFragmentFactory,
) {
    private val countryIsNotSelected: Boolean
        get() = settingsInteractor.userCountryCode == null

    fun navigateToAddMoney(sourceFragment: Fragment) = with(sourceFragment) {
        if (countryIsNotSelected) {
            val fragment = OnOffRampCountrySelectionFragment.create(NavigationDestination())
            replaceFragmentForResult(
                target = fragment,
                requestKey = OnOffRampCountrySelectionFragment.REQUEST_KEY,
                onResult = { _, bundle ->
                    if (bundle.getBoolean(OnOffRampCountrySelectionFragment.RESULT_KEY_COUNTRY_SELECTED, false)) {
                        showAddMoneyDialog(sourceFragment)
                    }
                    sourceFragment.clearResultListener()
                }
            )
        } else {
            showAddMoneyDialog(sourceFragment)
        }
    }

    /**
     * todo: this will be replaced with bottom sheet
     */
    fun navigateToWithdraw(sourceFragment: Fragment) = with(sourceFragment) {
        sourceFragment.clearResultListener()

        if (countryIsNotSelected) {
            val fragment = OnOffRampCountrySelectionFragment.create(
                NavigationDestination(
                    clazz = StrigaOffRampFragment::class.java,
                    args = null,
                    strategy = NavigationStrategy.Replace
                )
            )
            replaceFragment(fragment)
        } else {
            replaceFragment(strigaFragmentFactory.offRampFragment())
        }
    }

    private fun showAddMoneyDialog(sourceFragment: Fragment) = with(sourceFragment) {
        AddMoneyDialog.show(parentFragmentManager)
    }

    private fun Fragment.clearResultListener() {
        requireActivity()
            .supportFragmentManager
            .clearFragmentResultListener(OnOffRampCountrySelectionFragment.REQUEST_KEY)
    }
}
