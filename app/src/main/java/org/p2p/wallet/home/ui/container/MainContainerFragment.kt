package org.p2p.wallet.home.ui.container

import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import kotlin.reflect.KClass
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.ime
import org.p2p.core.utils.insets.systemBars
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.permissions.PermissionState
import org.p2p.wallet.common.permissions.new.requestPermissionNotification
import org.p2p.wallet.common.ui.BaseFragmentAdapter
import org.p2p.wallet.databinding.FragmentMainBinding
import org.p2p.wallet.deeplinks.DeeplinkData
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.deeplinks.MainTabsSwitcher
import org.p2p.wallet.home.ui.main.MainFragmentOnCreateAction
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.push_notifications.analytics.AnalyticsPushChannel
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.doOnAnimationEnd
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_MAIN_FRAGMENT_ACTIONS = "ARG_MAIN_FRAGMENT_ACTION"

class MainContainerFragment :
    BaseMvpFragment<MainContainerContract.View, MainContainerContract.Presenter>(R.layout.fragment_main),
    MainContainerContract.View,
    MainTabsSwitcher {

    override val presenter: MainContainerContract.Presenter by inject()

    private val binding: FragmentMainBinding by viewBinding()

    private var lastSelectedItemId = R.id.walletItem

    private lateinit var mainContainerAdapter: BaseFragmentAdapter
    private lateinit var fragmentsMap: Map<ScreenTab, KClass<out Fragment>>

    private val analytics: AnalyticsPushChannel by inject()

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

        presenter.initializeDeeplinks()
        presenter.observeUserTokens()

        requestPermissionNotification { permissionState ->
            if (permissionState == PermissionState.GRANTED) {
                analytics.pushPermissionsAllowed()
                AppNotificationManager.createNotificationChannels(requireContext())
            }
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
        if (binding.bottomNavigation.getSelectedItemId() != R.id.walletItem) {
            navigate(ScreenTab.WALLET_SCREEN)
        } else {
            requireActivity().finish()
        }
    }

    override fun showSettingsBadgeVisible(isVisible: Boolean) {
        binding.bottomNavigation.setBadgeVisible(screenTab = ScreenTab.SETTINGS_SCREEN, isVisible = isVisible)
    }

    override fun showCryptoBadgeVisible(isVisible: Boolean) {
    }

    override fun showWalletBadgeVisible(isVisible: Boolean) {
        binding.bottomNavigation.setBadgeVisible(screenTab = ScreenTab.WALLET_SCREEN, isVisible = isVisible)
    }

    override fun showWalletBalance(balance: String) {
        // todo: will this logic be used in the future?
        // binding.bottomNavigation.setMenuItemTitle(screenTab = ScreenTab.WALLET_SCREEN, balance)
        binding.bottomNavigation.setMenuItemTitle(
            screenTab = ScreenTab.WALLET_SCREEN,
            title = getString(R.string.common_crypto)
        )
    }

    override fun navigateToTabFromDeeplink(data: DeeplinkData) {
        when (data.target) {
            DeeplinkTarget.HISTORY -> navigate(ScreenTab.HISTORY_SCREEN)
            DeeplinkTarget.SETTINGS -> navigate(ScreenTab.SETTINGS_SCREEN)
            else -> Unit
        }
    }

    override fun navigate(clickedTab: ScreenTab) {
        when (clickedTab) {
            else -> {
                when (clickedTab) {
                    ScreenTab.WALLET_SCREEN -> presenter.logWalletOpened()
                    ScreenTab.HISTORY_SCREEN -> presenter.logHistoryOpened()
                    ScreenTab.SETTINGS_SCREEN -> presenter.logSettingsOpened()
                    else -> Unit
                }

                val itemId = clickedTab.itemId

                binding.viewPagerMainFragment.setCurrentItem(fragmentPositionByItemId(itemId), false)

                if (binding.bottomNavigation.getSelectedItemId() != itemId) {
                    binding.bottomNavigation.setChecked(itemId)
                } else {
                    checkAndDismissLastBottomSheet()
                }
            }
        }
    }

    private fun resetOnLastBottomNavigationItem() {
        with(binding.bottomNavigation) {
            post { // not working reselection on last item without post
                setChecked(lastSelectedItemId)
            }
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
                navigate(tab)
                return@setOnItemSelectedListener true
            }
        }
    }

    override fun inflateBottomNavigationMenu(menuRes: Int) {
        binding.bottomNavigation.menu.clear()
        binding.bottomNavigation.inflateMenu(menuRes)
    }

    private fun fragmentPositionByItemId(itemId: Int): Int {
        return fragmentsMap.keys.indexOfFirst { it.itemId == itemId }
    }
}
