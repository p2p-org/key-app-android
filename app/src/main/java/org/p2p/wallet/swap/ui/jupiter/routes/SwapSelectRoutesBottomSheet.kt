package org.p2p.wallet.swap.ui.jupiter.routes

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.koin.android.ext.android.inject
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.finance_block.UiKitFinanceBlockView
import org.p2p.uikit.components.finance_block.baseCellDelegate
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.ui.bottomsheet.BaseBottomSheet
import org.p2p.wallet.databinding.DialogSwapSelectRoutesBinding
import org.p2p.wallet.home.ui.main.bottomsheet.HomeActionsBottomSheet
import org.p2p.wallet.swap.jupiter.analytics.JupiterSwapSettingsAnalytics
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManagerHolder
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_STATE_MANAGE_KEY = "ARG_STATE_MANAGE_KEY"

class SwapSelectRoutesBottomSheet : BaseBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            stateManagerKey: String
        ) {
            val tag = HomeActionsBottomSheet::javaClass.name
            if (fm.findFragmentByTag(tag) != null) return
            SwapSelectRoutesBottomSheet()
                .withArgs(ARG_STATE_MANAGE_KEY to stateManagerKey)
                .show(fm, tag)
        }
    }

    private val binding: DialogSwapSelectRoutesBinding by viewBinding()
    private val stateManagerKey: String by args(ARG_STATE_MANAGE_KEY)
    private val managerHolder: SwapStateManagerHolder by inject()
    private val mapper: SwapSelectRoutesMapper by inject()
    private val analytics: JupiterSwapSettingsAnalytics by inject()
    private val stateManager: SwapStateManager
        get() = managerHolder.get(stateManagerKey)

    private val adapter = CommonAnyCellAdapter(
        baseCellDelegate(inflateListener = { financeBlock ->
            financeBlock.setOnClickAction { view, item -> onRouteClick(item, view) }
        }),
    )

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_swap_select_routes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewRoutes.attachAdapter(adapter)
        binding.buttonDone.setOnClickListener { dismiss() }
        lifecycleScope.launchWhenResumed {
            stateManager.observe().collect {
                adapter.items = getRoutesList(it)
            }
        }
    }

    private fun getRoutesList(state: SwapState): List<AnyCellItem> = when (state) {
        SwapState.InitialLoading,
        is SwapState.TokenAZero,
        is SwapState.LoadingRoutes -> mapper.mapLoadingList()
        is SwapState.LoadingTransaction -> mapper.mapRoutesList(state.routes, state.activeRoute, state.tokenB)
        is SwapState.SwapLoaded -> mapper.mapRoutesList(state.routes, state.activeRoute, state.tokenB)

        is SwapState.SwapException -> getRoutesList(state.previousFeatureState)
    }

    private fun onRouteClick(item: FinanceBlockCellModel, view: UiKitFinanceBlockView) {
        val route = (item.payload as? JupiterSwapRoute) ?: return
        analytics.logSwapRouteChanged(route)

        val routePosition = binding.recyclerViewRoutes.findContainingViewHolder(view)?.bindingAdapterPosition ?: return
        stateManager.onNewAction(SwapStateAction.ActiveRouteChanged(routePosition))
        dismiss()
        // for now expecting JupiterSwapSettingsFragment behind, else ref to result api
        popBackStack()
    }
}
