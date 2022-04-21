package org.p2p.wallet.home

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.collection.SparseArrayCompat
import androidx.collection.set
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentMainBinding
import org.p2p.wallet.home.ui.main.HomeFragment
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.send.ui.main.SendFragment
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class MainFragment : BaseFragment(R.layout.fragment_main) {

    private val binding: FragmentMainBinding by viewBinding()
    private val fragments = SparseArrayCompat<Fragment>()
    private val analyticsInteractor: ScreensAnalyticsInteractor by inject()

    companion object {
        fun create(): MainFragment = MainFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (binding.bottomNavigation.selectedItemId != R.id.itemHome) {
                navigate(R.id.itemHome)
                binding.bottomNavigation.menu[0].isChecked = true
            } else {
                requireActivity().finish()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            bottomNavigation.setOnItemSelectedListener {
                if (it.itemId == R.id.itemFeedback) {
                    IntercomService.showMessenger()
                    analyticsInteractor.logScreenOpenEvent(ScreenNames.Main.MAIN_FEEDBACK)
                    return@setOnItemSelectedListener false
                }
                navigate(it.itemId)
                return@setOnItemSelectedListener true
            }
        }

        if (fragments.isEmpty) {
            childFragmentManager.fragments.forEach { fragment ->
                when (fragment) {
                    is HomeFragment -> fragments.put(R.id.itemHome, fragment)
                    is SendFragment -> fragments.put(R.id.itemSend, fragment)
                    is SettingsFragment -> fragments.put(R.id.itemSettings, fragment)
                }
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.itemHome
    }

    private fun navigate(itemId: Int) {
        if (!fragments.containsKey(itemId)) {
            val fragment = when (Tabs.fromTabId(itemId)) {
                Tabs.HOME -> {
                    analyticsInteractor.logScreenOpenEvent(ScreenNames.Main.MAIN_COINS)
                    HomeFragment.create()
                }
                Tabs.SEND -> {
                    analyticsInteractor.logScreenOpenEvent(ScreenNames.Send.MAIN)
                    SendFragment.create()
                }
                Tabs.SETTINGS -> {
                    analyticsInteractor.logScreenOpenEvent(ScreenNames.Settings.MAIN)
                    SettingsFragment.create()
                }
            }
            fragments[itemId] = fragment
        }

        val prevFragmentId = binding.bottomNavigation.selectedItemId
        childFragmentManager.commit(allowStateLoss = true) {
            if (prevFragmentId != itemId) {
                if (fragments[prevFragmentId] != null && !fragments[prevFragmentId]!!.isAdded) {
                    remove(fragments[prevFragmentId]!!)
                } else if (fragments[prevFragmentId] != null) {
                    hide(fragments[prevFragmentId]!!)
                    setMaxLifecycle(fragments[prevFragmentId]!!, Lifecycle.State.CREATED)
                }
            }
            val nextFragmentTag = fragments[itemId]!!.javaClass.name + "_" + itemId
            if (childFragmentManager.findFragmentByTag(nextFragmentTag) == null) {
                if (fragments[itemId]!!.isAdded) {
                    return
                }
                add(R.id.fragmentContainer, fragments[itemId]!!, nextFragmentTag)
            } else {
                if (!fragments[itemId]!!.isAdded) {
                    remove(fragments[itemId]!!)
                    if (fragments[itemId]!!.isAdded) {
                        return
                    }
                    add(R.id.fragmentContainer, fragments[itemId]!!, nextFragmentTag)
                } else {
                    show(fragments[itemId]!!)
                    setMaxLifecycle(fragments[itemId]!!, Lifecycle.State.RESUMED)
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        showUI()
    }

    private fun showUI() {
        binding.bottomNavigation.menu.clear()
        binding.bottomNavigation.inflateMenu(R.menu.main)
    }
}

private enum class Tabs(val tabId: Int) {
    HOME(R.id.itemHome),
    SEND(R.id.itemSend),
    SETTINGS(R.id.itemSettings);

    companion object {
        fun fromTabId(tabId: Int): Tabs {
            return values()
                .firstOrNull { it.tabId == tabId }
                ?: throw IllegalArgumentException("Unknown tabId=$tabId")
        }
    }
}
