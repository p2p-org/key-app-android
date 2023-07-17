package org.p2p.wallet.home.ui.container

import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import kotlin.reflect.KClass
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.ime
import org.p2p.core.utils.insets.systemBars
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.GeneralAnalytics
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.BaseFragmentAdapter
import org.p2p.wallet.databinding.FragmentMainBinding
import org.p2p.wallet.deeplinks.DeeplinkData
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.deeplinks.MainTabsSwitcher
import org.p2p.wallet.home.ui.main.MainFragmentOnCreateAction
import org.p2p.wallet.home.ui.select.bottomsheet.SelectTokenBottomSheet
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.jupiter.ui.main.JupiterSwapFragment
import org.p2p.wallet.moonpay.ui.BuyFragmentFactory
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.newsend.ui.SearchOpenedFromScreen
import org.p2p.wallet.newsend.ui.search.NewSearchFragment
import org.p2p.wallet.newsend.ui.stub.SendUnavailableFragment
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.sell.ui.payload.SellPayloadFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.doOnAnimationEnd
import org.p2p.wallet.utils.getParcelableCompat
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_MAIN_FRAGMENT_ACTIONS = "ARG_MAIN_FRAGMENT_ACTION"

private const val KEY_RESULT_TOKEN = "KEY_RESULT_TOKEN"
private const val KEY_REQUEST_TOKEN = "KEY_REQUEST_TOKEN"

class MainContainerFragment :
    BaseMvpFragment<MainContainerContract.View, MainContainerContract.Presenter>(R.layout.fragment_main),
    MainContainerContract.View,
    MainTabsSwitcher {

    override val presenter: MainContainerContract.Presenter by inject()

    private val binding: FragmentMainBinding by viewBinding()

    private val generalAnalytics: GeneralAnalytics by inject()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    private val sellInteractor: SellInteractor by inject()

    private var lastSelectedItemId = R.id.walletItem

    private lateinit var mainContainerAdapter: BaseFragmentAdapter
    private lateinit var fragmentsMap: Map<ScreenTab, KClass<out Fragment>>

    private val buyFragmentFactory: BuyFragmentFactory by inject()

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
            buttonCenterAction.setOnClickListener {
                lifecycleScope.launch {
                    generalAnalytics.logActionButtonClicked(
                        lastScreenName = analyticsInteractor.getCurrentScreenName(),
                        isSellEnabled = sellInteractor.isSellAvailable()
                    )
                }
                presenter.onSendClicked()
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

        childFragmentManager.setFragmentResultListener(
            KEY_REQUEST_TOKEN,
            viewLifecycleOwner,
            ::onFragmentResult
        )

        presenter.initializeDeeplinks()
        presenter.observeUserTokens()
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
        binding.bottomNavigation.setBadgeVisible(screenTab = ScreenTab.MY_CRYPTO_SCREEN, isVisible = isVisible)
    }

    override fun showWalletBalance(balance: String) {
        binding.bottomNavigation.setMenuItemTitle(screenTab = ScreenTab.WALLET_SCREEN, balance)
    }

    override fun navigateToSendNoTokens(fallbackToken: Token) {
        replaceFragment(SendUnavailableFragment.create(fallbackToken))
    }

    override fun navigateToSendScreen() {
        replaceFragment(NewSearchFragment.create(SearchOpenedFromScreen.MAIN))
    }

    //region DeeplinkScreenNavigator
    override fun navigateToNewBuyScreen(token: Token, fiatToken: String, fiatAmount: String?) {
        replaceFragment(NewBuyFragment.create(token, fiatToken, fiatAmount))
    }

    override fun navigateToBuyScreen(token: Token) {
        replaceFragment(buyFragmentFactory.buyFragment(token))
    }

    override fun showTokensForBuy(tokens: List<Token>) {
        SelectTokenBottomSheet.show(
            fm = childFragmentManager,
            tokens = tokens,
            requestKey = KEY_REQUEST_TOKEN,
            resultKey = KEY_RESULT_TOKEN
        )
    }

    override fun showCashOut() {
        replaceFragment(SellPayloadFragment.create())
    }

    override fun showSwapWithArgs(tokenASymbol: String, tokenBSymbol: String, amountA: String, source: SwapOpenedFrom) {
        replaceFragment(
            JupiterSwapFragment.create(tokenASymbol, tokenBSymbol, amountA, source)
        )
    }

    override fun showSwap() {
        replaceFragment(JupiterSwapFragment.create(source = SwapOpenedFrom.MAIN_SCREEN))
    }
    //endregion

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        if (requestKey == KEY_REQUEST_TOKEN) {
            result.getParcelableCompat<Token>(KEY_RESULT_TOKEN)?.also(::navigateToBuyScreen)
        }
    }

    override fun navigateFromDeeplink(data: DeeplinkData) {
        when (data.target) {
            DeeplinkTarget.MY_CRYPTO -> {
                navigate(ScreenTab.MY_CRYPTO_SCREEN)
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
        when (clickedTab) {
            ScreenTab.SEND_SCREEN -> {
                presenter.onSendClicked()
                resetOnLastBottomNavigationItem()
                return
            }
            else -> {
                when (clickedTab) {
                    ScreenTab.MY_CRYPTO_SCREEN -> presenter.logHomeOpened()
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
        binding.bottomNavigation.menu.findItem(R.id.sendItem)?.isCheckable = false
    }

    private fun fragmentPositionByItemId(itemId: Int): Int {
        return fragmentsMap.keys.indexOfFirst { it.itemId == itemId }
    }
}
