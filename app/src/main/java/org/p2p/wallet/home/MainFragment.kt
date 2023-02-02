package org.p2p.wallet.home

import androidx.activity.addCallback
import androidx.collection.SparseArrayCompat
import androidx.collection.set
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.ime
import org.p2p.core.utils.insets.systemBars
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.GeneralAnalytics
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SolendEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentMainBinding
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.deeplinks.CenterActionButtonClickSetter
import org.p2p.wallet.deeplinks.MainTabsSwitcher
import org.p2p.wallet.home.ui.main.HomeFragment
import org.p2p.wallet.home.ui.main.MainFragmentOnCreateAction
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.settings.ui.settings.NewSettingsFragment
import org.p2p.wallet.solend.ui.earn.SolendEarnFragment
import org.p2p.wallet.solend.ui.earn.StubSolendEarnFragment
import org.p2p.wallet.swap.analytics.SwapAnalytics
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapOpenedFrom
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.doOnAnimationEnd
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import kotlinx.coroutines.launch
import org.p2p.wallet.history.ui.new_history.NewHistoryFragment

private const val ARG_MAIN_FRAGMENT_ACTIONS = "ARG_MAIN_FRAGMENT_ACTION"

class MainFragment :
    BaseFragment(R.layout.fragment_main),
    MainTabsSwitcher,
    CenterActionButtonClickSetter {

    private val binding: FragmentMainBinding by viewBinding()

    private val tabCachedFragments = SparseArrayCompat<Fragment>()

    private val generalAnalytics: GeneralAnalytics by inject()
    private val swapAnalytics: SwapAnalytics by inject()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val deeplinksManager: AppDeeplinksManager by inject()
    private val solendFeatureToggle: SolendEnabledFeatureToggle by inject()
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle by inject()

    private val sellInteractor: SellInteractor by inject()

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

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { view, insets, initialPadding ->
            val systemBars = insets.systemBars()
            view.updatePadding(
                left = initialPadding.left + systemBars.left,
                top = 0,
                right = initialPadding.right + systemBars.right,
                bottom = initialPadding.bottom + systemBars.bottom,
            )
            val ime = insets.ime()
            val bottomNavigationHeight = binding.bottomNavigation.height
            val bottomConsume = if (ime.bottom > bottomNavigationHeight) bottomNavigationHeight else ime.bottom
            insets.inset(
                systemBars.left,
                0,
                systemBars.right,
                systemBars.bottom + bottomConsume,
            )
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
                is NewHistoryFragment -> tabCachedFragments.put(R.id.historyItem, fragment)
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
        if (clickedTab == ScreenTab.FEEDBACK_SCREEN) {
            IntercomService.showMessenger()
            analyticsInteractor.logScreenOpenEvent(ScreenNames.Main.MAIN_FEEDBACK)
            with(binding.bottomNavigation) {
                post { // not working reselection on last item without post
                    setChecked(lastSelectedItemId)
                }
            }
            return
        }

        if (clickedTab == ScreenTab.SWAP_SCREEN) {
            lifecycleScope.launch {
                swapAnalytics.logSwapOpenedFromMain(sellInteractor.isSellAvailable())
            }
        }

        val itemId = clickedTab.itemId

        // fixme: https://p2pvalidator.atlassian.net/browse/PWN-7051 Refreshing swap every time
        if (clickedTab == ScreenTab.SWAP_SCREEN) {
            tabCachedFragments.remove(itemId)
        }

        if (!tabCachedFragments.containsKey(clickedTab.itemId)) {
            val fragment = when (clickedTab) {
                ScreenTab.HOME_SCREEN -> HomeFragment.create()
                ScreenTab.EARN_SCREEN -> StubSolendEarnFragment.create()
                ScreenTab.HISTORY_SCREEN -> NewHistoryFragment.create()
                ScreenTab.SETTINGS_SCREEN -> NewSettingsFragment.create()
                ScreenTab.SWAP_SCREEN -> OrcaSwapFragment.create(OrcaSwapOpenedFrom.MAIN_SCREEN)
                else -> error("Can't create fragment for $clickedTab")
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

        val menuRes = when {
            solendFeatureToggle.isFeatureEnabled -> R.menu.menu_ui_kit_bottom_navigation_earn
            sellEnabledFeatureToggle.isFeatureEnabled -> R.menu.menu_ui_kit_bottom_navigation_sell
            else -> R.menu.menu_ui_kit_bottom_navigation
        }
        binding.bottomNavigation.inflateMenu(menuRes)
        binding.bottomNavigation.menu.findItem(R.id.feedbackItem)?.isCheckable = false
    }

    override fun setOnCenterActionButtonListener(block: () -> Unit) {
        binding.buttonCenterAction.setOnClickListener {
            lifecycleScope.launch {
                generalAnalytics.logActionButtonClicked(
                    lastScreenName = analyticsInteractor.getCurrentScreenName(),
                    isSellEnabled = sellInteractor.isSellAvailable()
                )
            }
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
