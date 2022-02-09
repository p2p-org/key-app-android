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
import org.p2p.wallet.R
import org.p2p.wallet.databinding.FragmentHomeBinding
import org.p2p.wallet.main.ui.main.MainFragment
import org.p2p.wallet.main.ui.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.main.ui.send.SendFragment
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.utils.edgetoedge.Edge
import org.p2p.wallet.utils.edgetoedge.edgeToEdge
import org.p2p.wallet.utils.viewbinding.viewBinding

class MainHomeFragment : Fragment(R.layout.fragment_home) {
    private val binding: FragmentHomeBinding by viewBinding()
    private val fragments = SparseArrayCompat<Fragment>()

    companion object {
        fun create(): MainHomeFragment = MainHomeFragment()
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
            edgeToEdge {
                contentView.fitPadding { Edge.All }
            }
            bottomNavigation.setOnItemSelectedListener {
                navigate(it.itemId)
                return@setOnItemSelectedListener true
            }
        }

        // После activity.restart() поле очищается, но фрагменты есть в fragmentManager
        if (fragments.isEmpty) {
            childFragmentManager.fragments.forEach { fragment ->
                when (fragment) {
                    is MainFragment -> fragments.put(R.id.itemHome, fragment)
                    is SendFragment -> fragments.put(R.id.itemSend, fragment)
                    is ReceiveSolanaFragment -> fragments.put(R.id.itemFeedback, fragment)
                    is SettingsFragment -> fragments.put(R.id.itemSettings, fragment)
                }
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.itemHome
    }

    private fun navigate(itemId: Int) {
        if (!fragments.containsKey(itemId)) {
            val fragment = when (Tabs.fromTabId(itemId)) {
                Tabs.HOME -> MainFragment.create()
                Tabs.SEND -> SendFragment.create()
                Tabs.FEEDBACK -> ReceiveSolanaFragment.create(null)
                Tabs.SETTINGS -> SettingsFragment.create()
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
                add(R.id.container, fragments[itemId]!!, nextFragmentTag)
            } else {
                if (!fragments[itemId]!!.isAdded) {
                    // После смены языка (activity.recreate()) фрагмент как бы есть в childFragmentManager,
                    // но он не работает. Переприсоединяем.
                    remove(fragments[itemId]!!)
                    if (fragments[itemId]!!.isAdded) {
                        return
                    }
                    add(R.id.container, fragments[itemId]!!, nextFragmentTag)
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
    FEEDBACK(R.id.itemFeedback),
    SETTINGS(R.id.itemSettings);

    companion object {
        fun fromTabId(tabId: Int): Tabs {
            return values()
                .firstOrNull { it.tabId == tabId }
                ?: throw IllegalArgumentException("Unknown tabId=$tabId")
        }
    }
}