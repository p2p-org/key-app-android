package org.p2p.wallet.swap.ui.jupiter.info

import androidx.annotation.DrawableRes
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.koin.android.ext.android.inject
import org.p2p.uikit.components.finance_block.baseCellDelegate
import org.p2p.uikit.components.info_block.infoBlockCellDelegate
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.bottomsheet.BaseBottomSheet
import org.p2p.wallet.databinding.DialogSwapInfoBinding
import org.p2p.wallet.databinding.ItemSwapInfoBannerBinding
import org.p2p.wallet.swap.jupiter.interactor.SwapTokensInteractor
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManagerHolder
import org.p2p.wallet.swap.ui.jupiter.main.SwapRateLoaderState
import org.p2p.wallet.swap.ui.jupiter.settings.adapter.SwapSettingsAdapter
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_STATE_MANAGE_KEY = "ARG_STATE_MANAGE_KEY"
private const val ARG_INFO_TYPE_KEY = "ARG_INFO_TYPE_KEY"

enum class SwapInfoType {
    NETWORK_FEE, ACCOUNT_FEE, LIQUIDITY_FEE, MINIMUM_RECEIVED
}

class SwapInfoBottomSheet : BaseBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            stateManagerKey: String,
            swapInfoType: SwapInfoType,
        ) {
            val tag = SwapInfoBottomSheet::javaClass.name
            if (fm.findFragmentByTag(tag) != null) return
            SwapInfoBottomSheet()
                .withArgs(
                    ARG_STATE_MANAGE_KEY to stateManagerKey,
                    ARG_INFO_TYPE_KEY to swapInfoType,
                )
                .show(fm, tag)
        }
    }

    private val binding: DialogSwapInfoBinding by viewBinding()
    private val stateManagerKey: String by args(ARG_STATE_MANAGE_KEY)
    private val swapInfoType: SwapInfoType by args(ARG_INFO_TYPE_KEY)

    private val managerHolder: SwapStateManagerHolder by inject()
    private val mapper: SwapInfoMapper by inject()
    private val interactor: SwapTokensInteractor by inject()
    private val stateManager: SwapStateManager
        get() = managerHolder.get(stateManagerKey)

    private val adapter = SwapSettingsAdapter(
        baseCellDelegate(),
        infoBlockCellDelegate(),
        swapInfoBannerDelegate(),
    )

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_swap_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewRoutes.attachAdapter(adapter)
        binding.buttonDone.setOnClickListener { dismiss() }
        lifecycleScope.launchWhenResumed {
            when (swapInfoType) {
                SwapInfoType.MINIMUM_RECEIVED -> {
                    adapter.items = mapper.mapMinimumReceived()
                    binding.buttonDone.setText(R.string.swap_info_details_minimum_received_fee_done)
                }
                SwapInfoType.NETWORK_FEE -> {
                    adapter.items = mapper.mapNetworkFee()
                    binding.buttonDone.setText(R.string.swap_info_details_network_fee_done)
                }
                SwapInfoType.ACCOUNT_FEE -> {
                    adapter.items = mapper.mapAccountFee()
                    binding.buttonDone.setText(R.string.swap_info_details_account_fee_done)
                }
                SwapInfoType.LIQUIDITY_FEE -> {
                    binding.buttonDone.setText(R.string.swap_info_details_liquidity_fee_done)
                    stateManager.observe().collect(::handleFeatureState)
                }
            }
        }
    }

    private suspend fun handleFeatureState(state: SwapState) {
        val allTokens = interactor.getAllTokens()

        when (state) {
            SwapState.InitialLoading -> mapper.mapLiquidityFee(allTokens)
            is SwapState.LoadingRoutes -> mapper.mapLiquidityFee(allTokens)
            is SwapState.TokenAZero -> mapper.mapLiquidityFee(allTokens)
            is SwapState.SwapException -> handleFeatureState(state.previousFeatureState)

            is SwapState.LoadingTransaction -> mapper.mapLiquidityFee(
                allTokens = allTokens,
                route = state.routes.getOrNull(state.activeRoute)
            )
            is SwapState.SwapLoaded -> mapper.mapLiquidityFee(
                allTokens = allTokens,
                route = state.routes.getOrNull(state.activeRoute)
            )
        }
    }

    private suspend fun loadRate(
        allTokens: List<SwapTokenModel>,
        route: JupiterSwapRoute? = null,
    ) {
        if (route == null) return
        route.marketInfos.map { marketInfo ->
            val lpToken = allTokens.find { marketInfo.lpFee.mint == it.mintAddress } ?: return@map null
            stateManager.getTokenRate(lpToken)
        }.mapNotNull { it }
            .filterIsInstance<SwapRateLoaderState.NoRateAvailable>()
    }

    private fun List<JupiterSwapToken>.findTokenByMint(mint: Base58String): JupiterSwapToken? {
        return find { it.tokenMint == mint }
    }
}

fun swapInfoBannerDelegate(): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<SwapInfoBannerCellModel, AnyCellItem, ItemSwapInfoBannerBinding>(
        viewBinding = { inflater, parent -> ItemSwapInfoBannerBinding.inflate(inflater, parent, false) }
    ) {

        bind {
            binding.imageViewBanner.setImageResource(item.banner)
        }
    }

data class SwapInfoBannerCellModel(
    @DrawableRes val banner: Int,
) : AnyCellItem
