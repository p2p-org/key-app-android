package org.p2p.wallet.home

import androidx.activity.addCallback
import androidx.collection.SparseArrayCompat
import androidx.collection.set
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.GeneralAnalytics
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.SolendEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentMainBinding
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.deeplinks.CenterActionButtonClickSetter
import org.p2p.wallet.deeplinks.MainTabsSwitcher
import org.p2p.wallet.history.ui.history.HistoryFragment
import org.p2p.wallet.home.ui.main.HomeFragment
import org.p2p.wallet.home.ui.main.MainFragmentOnCreateAction
import org.p2p.wallet.settings.ui.settings.NewSettingsFragment
import org.p2p.wallet.solend.ui.earn.SolendEarnFragment
import org.p2p.wallet.solend.ui.earn.StubSolendEarnFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapOpenedFrom
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.doOnAnimationEnd
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_MAIN_FRAGMENT_ACTIONS = "ARG_MAIN_FRAGMENT_ACTION"

class MainFragment : BaseFragment(R.layout.fragment_main), MainTabsSwitcher, CenterActionButtonClickSetter {

    private val binding: FragmentMainBinding by viewBinding()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()
    private val tabCachedFragments = SparseArrayCompat<Fragment>()
    private val generalAnalytics: GeneralAnalytics by inject()
    private val deeplinksManager: AppDeeplinksManager by inject()
    private val solendFeatureToggle: SolendEnabledFeatureToggle by inject()

    private var lastSelectedItemId = R.id.homeItem

    companion object {
        fun create(actions: ArrayList<MainFragmentOnCreateAction> = arrayListOf()): MainFragment =
            MainFragment()
                .withArgs(ARG_MAIN_FRAGMENT_ACTIONS to actions)
    }

    private var onCreateActions: ArrayList<MainFragmentOnCreateAction>? by args(
        key = ARG_MAIN_FRAGMENT_ACTIONS, defaultValue = arrayListOf()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deeplinksManager.mainTabsSwitcher = this

        with(binding) {
            bottomNavigation.setOnItemSelectedListener { tab ->
                triggerTokensUpdateIfNeeded()
                navigate(tab)
                return@setOnItemSelectedListener true
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onActivityBackPressed()
        }

        inflateBottomNavigation()

        if (tabCachedFragments.isEmpty) {
            createCachedTabFragments()
        }
        deeplinksManager.handleSavedDeeplinkIntent()

        if (onCreateActions?.isNotEmpty() == true) {
            onCreateActions?.forEach(::doOnCreateAction)
            onCreateActions = arrayListOf()
        }
    }

    private fun doOnCreateAction(action: MainFragmentOnCreateAction) {
        when (action) {
            is MainFragmentOnCreateAction.ShowSnackbar -> {
                showUiKitSnackBar(messageResId = action.messageResId)
            }
            is MainFragmentOnCreateAction.PlayAnimation -> {
                with(binding.animationView) {
                    setAnimation(action.animationRes)
                    isVisible = true
                    doOnAnimationEnd { isVisible = false }
                    playAnimation()
                }
            }
        }
    }

    private fun onActivityBackPressed() {
        if (binding.bottomNavigation.getSelectedItemId() != R.id.homeItem) {
            navigate(ScreenTab.HOME_SCREEN)
        } else {
            requireActivity().finish()
        }
    }

    private fun createCachedTabFragments() {
        childFragmentManager.fragments.forEach { fragment ->
            when (fragment) {
                is HomeFragment -> tabCachedFragments.put(R.id.homeItem, fragment)
                is HistoryFragment -> tabCachedFragments.put(R.id.historyItem, fragment)
                is NewSettingsFragment -> tabCachedFragments.put(R.id.settingsItem, fragment)
                is SolendEarnFragment -> tabCachedFragments.put(R.id.earnItem, fragment)
                is OrcaSwapFragment -> tabCachedFragments.put(R.id.swapItem, fragment)
            }
        }
        binding.bottomNavigation.setSelectedItemId(R.id.homeItem)
    }

    override fun onDestroyView() {
        deeplinksManager.mainTabsSwitcher = null
        super.onDestroyView()
    }

    override fun navigate(clickedTab: ScreenTab) {
        val itemId = clickedTab.itemId

        if (!tabCachedFragments.containsKey(clickedTab.itemId)) {
            val fragment = when (clickedTab) {
                ScreenTab.HOME_SCREEN -> HomeFragment.create()
                ScreenTab.EARN_SCREEN -> StubSolendEarnFragment.create()
                ScreenTab.HISTORY_SCREEN -> HistoryFragment.create()
                ScreenTab.SETTINGS_SCREEN -> NewSettingsFragment.create()
                ScreenTab.SWAP_SCREEN -> OrcaSwapFragment.create(OrcaSwapOpenedFrom.MAIN_SCREEN)
            }
            tabCachedFragments[itemId] = fragment
        }

        val prevFragmentId = binding.bottomNavigation.getSelectedItemId()
        childFragmentManager.commit(allowStateLoss = false) {
            if (prevFragmentId != itemId) {
                if (tabCachedFragments[prevFragmentId] != null && !tabCachedFragments[prevFragmentId]!!.isAdded) {
                    remove(tabCachedFragments[prevFragmentId]!!)
                } else if (tabCachedFragments[prevFragmentId] != null) {
                    hide(tabCachedFragments[prevFragmentId]!!)
                }
            }
            val nextFragmentTag = tabCachedFragments[itemId]!!.javaClass.name
            if (childFragmentManager.findFragmentByTag(nextFragmentTag) == null) {

                if (tabCachedFragments[itemId]!!.isAdded) {
                    return
                }
                add(R.id.fragmentContainer, tabCachedFragments[itemId]!!, nextFragmentTag)
            } else {
                if (!tabCachedFragments[itemId]!!.isAdded) {
                    remove(tabCachedFragments[itemId]!!)
                    if (tabCachedFragments[itemId]!!.isAdded) {
                        return
                    }
                    add(R.id.fragmentContainer, tabCachedFragments[itemId]!!, nextFragmentTag)
                } else {
                    show(tabCachedFragments[itemId]!!)
                }
            }
        }
        if (binding.bottomNavigation.getSelectedItemId() != itemId) {
            binding.bottomNavigation.setChecked(itemId)
        } else {
            checkAndDismissLastBottomSheet()
        }
        lastSelectedItemId = itemId
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        inflateBottomNavigation()
    }

    private fun checkAndDismissLastBottomSheet() {
        childFragmentManager.apply {
            if (fragments.size > 2) {
                val lastScreen = fragments.lastOrNull()
                if (lastScreen is BottomSheetDialogFragment) {
                    lastScreen.dismissAllowingStateLoss()
                }
            }
        }
    }

    private fun inflateBottomNavigation() {
        binding.bottomNavigation.menu.clear()

        val menuRes = if (solendFeatureToggle.isFeatureEnabled) {
            R.menu.menu_ui_kit_bottom_navigation_earn
        } else {
            R.menu.menu_ui_kit_bottom_navigation
        }
        binding.bottomNavigation.inflateMenu(menuRes)
    }

    override fun setOnCenterActionButtonListener(block: () -> Unit) {
        binding.buttonCenterAction.setOnClickListener {
            generalAnalytics.logActionButtonClicked(analyticsInteractor.getCurrentScreenName())
            block.invoke()
        }
    }

    // TODO: this is a dirty hack on how to trigger data update
    // Find a good solution for tracking the KEY_HIDDEN_ZERO_BALANCE value in SP
    private fun triggerTokensUpdateIfNeeded() {
        val fragment = tabCachedFragments[R.id.homeItem] as? HomeFragment ?: return
        fragment.updateTokensIfNeeded()
    }
}
