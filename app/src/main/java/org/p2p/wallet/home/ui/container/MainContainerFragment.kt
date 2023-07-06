package org.p2p.wallet.home.ui.container

import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import kotlin.reflect.KClass
import kotlinx.coroutines.launch
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.ime
import org.p2p.core.utils.insets.systemBars
import org.p2p.core.utils.launchRestartable
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.GeneralAnalytics
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.BaseFragmentAdapter
import org.p2p.wallet.databinding.FragmentMainBinding
import org.p2p.wallet.deeplinks.CenterActionButtonClickSetter
import org.p2p.wallet.deeplinks.DeeplinkData
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.deeplinks.MainTabsSwitcher
import org.p2p.wallet.home.ui.main.HomeFragment
import org.p2p.wallet.home.ui.main.MainFragmentOnCreateAction
import org.p2p.wallet.home.ui.main.RefreshErrorFragment
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.doOnAnimationEnd
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_MAIN_FRAGMENT_ACTIONS = "ARG_MAIN_FRAGMENT_ACTION"

class MainContainerFragment :
    BaseMvpFragment<MainContainerContract.View, MainContainerContract.Presenter>(R.layout.fragment_main),
    MainContainerContract.View,
    MainTabsSwitcher,
    CenterActionButtonClickSetter {

    override val presenter: MainContainerContract.Presenter by inject()

    private val binding: FragmentMainBinding by viewBinding()

    private val generalAnalytics: GeneralAnalytics by inject()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val sellInteractor: SellInteractor by inject()

    private var lastSelectedItemId = R.id.homeItem

    private lateinit var mainContainerAdapter: BaseFragmentAdapter
    private lateinit var fragmentsMap: Map<ScreenTab, KClass<out Fragment>>

    private val refreshErrorFragment: RefreshErrorFragment by unsafeLazy {
        RefreshErrorFragment()
    }

    companion object {
        fun create(actions: ArrayList<MainFragmentOnCreateAction> = arrayListOf()): MainContainerFragment =
            MainContainerFragment()
                .withArgs(ARG_MAIN_FRAGMENT_ACTIONS to actions)
    }

    private var onCreateActions: ArrayList<MainFragmentOnCreateAction>? by args(
        key = ARG_MAIN_FRAGMENT_ACTIONS, defaultValue = arrayListOf()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.loadMainNavigation()
        with(binding) {
            viewPagerMainFragment.isUserInputEnabled = false
            bottomNavigation.setOnItemSelectedListener { tab ->
                triggerTokensUpdateIfNeeded()
                navigate(tab)
                return@setOnItemSelectedListener true
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onActivityBackPressed()
        }

        presenter.loadBottomNavigationMenu()

        if (onCreateActions?.isNotEmpty() == true) {
            onCreateActions?.forEach(::doOnCreateAction)
            onCreateActions = arrayListOf()
        }

        parentFragmentManager.commit {
            if (!refreshErrorFragment.isAdded) {
                add(R.id.rootContainer, refreshErrorFragment)
            }
            hide(refreshErrorFragment)
        }

        lifecycle.launchRestartable {
            presenter.launchInternetObserver(this)
        }

        presenter.initializeDeeplinks()
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

    override fun showConnectionError(isVisible: Boolean) {
        parentFragmentManager.commit {
            if (isVisible) {
                show(refreshErrorFragment)
                hide(this@MainContainerFragment)
            } else {
                show(this@MainContainerFragment)
                hide(refreshErrorFragment)
            }
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

    override fun showSettingsBadgeVisible(isVisible: Boolean) {
        binding.bottomNavigation.setBadgeVisible(isVisible = isVisible)
    }

    override fun navigateFromDeeplink(data: DeeplinkData) {
        when (data.target) {
            DeeplinkTarget.HOME -> {
                navigate(ScreenTab.HOME_SCREEN)
            }

            DeeplinkTarget.HISTORY -> {
                navigate(ScreenTab.HISTORY_SCREEN)
            }

            DeeplinkTarget.SETTINGS -> {
                navigate(ScreenTab.SETTINGS_SCREEN)
            }

            else -> Unit
        }
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
        when (clickedTab) {
            ScreenTab.HOME_SCREEN -> presenter.logHomeOpened()
            ScreenTab.EARN_SCREEN -> presenter.logEarnOpened()
            ScreenTab.HISTORY_SCREEN -> presenter.logHistoryOpened()
            ScreenTab.SWAP_SCREEN -> presenter.logSwapOpened()
            ScreenTab.SETTINGS_SCREEN -> presenter.logSettingsOpened()
            else -> Unit
        }

        if (clickedTab == ScreenTab.SWAP_SCREEN) {
            presenter.logSwapOpened()
        }

        val itemId = clickedTab.itemId

        binding.viewPagerMainFragment.setCurrentItem(fragmentPositionByItemId(itemId), false)

        if (binding.bottomNavigation.getSelectedItemId() != itemId) {
            binding.bottomNavigation.setChecked(itemId)
        } else {
            checkAndDismissLastBottomSheet()
        }
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

    override fun setMainNavigationConfiguration(screensConfiguration: List<ScreenConfiguration>) {
        fragmentsMap = screensConfiguration.associate { it.screen to it.kClass }
        mainContainerAdapter = BaseFragmentAdapter(
            fragmentManager = childFragmentManager,
            lifecycle = lifecycle,
            pages = screensConfiguration.map { it.toFragmentPage() }
        )
        with(binding) {
            viewPagerMainFragment.adapter = mainContainerAdapter
            viewPagerMainFragment.isUserInputEnabled = false
            bottomNavigation.setOnItemSelectedListener { tab ->
                triggerTokensUpdateIfNeeded()
                navigate(tab)
                return@setOnItemSelectedListener true
            }
        }
    }

    override fun inflateBottomNavigationMenu(menuRes: Int) {
        binding.bottomNavigation.menu.clear()
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
        val homeItemId = ScreenTab.HOME_SCREEN.itemId
        val foundFragment = mainContainerAdapter.fragments[fragmentPositionByItemId(homeItemId)]
        val fragment = foundFragment as? HomeFragment ?: return
        fragment.updateTokensIfNeeded()
    }

    private fun fragmentPositionByItemId(itemId: Int): Int {
        return fragmentsMap.keys.indexOfFirst { it.itemId == itemId }
    }
}
