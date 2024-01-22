package org.p2p.wallet.jupiter.ui.info

import androidx.annotation.DrawableRes
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.view.View
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.baseCellDelegate
import org.p2p.uikit.components.info_block.InfoBlockCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.ui.bottomsheet.BaseBottomSheet
import org.p2p.wallet.databinding.DialogSwapInfoBinding
import org.p2p.wallet.databinding.ItemSwapInfoBannerBinding
import org.p2p.wallet.jupiter.interactor.SwapTokensInteractor
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoutePlanV6
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.jupiter.statemanager.SwapStateManagerHolder
import org.p2p.wallet.jupiter.ui.main.SwapRateLoaderState
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_STATE_MANAGE_KEY = "ARG_STATE_MANAGE_KEY"
private const val ARG_INFO_TYPE_KEY = "ARG_INFO_TYPE_KEY"

enum class SwapInfoType {
    NETWORK_FEE, ACCOUNT_FEE, LIQUIDITY_FEE, MINIMUM_RECEIVED
}

private typealias LoadRateBox = Triple<JupiterSwapRoutePlanV6, MainCellModel, SwapRateLoaderState>

class SwapInfoBottomSheet : BaseBottomSheet(R.layout.dialog_swap_info) {

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
    private val interactor: SwapTokensInteractor by inject {
        parametersOf(stateManagerKey)
    }

    private val swapTokensRepository: JupiterSwapTokensRepository by inject()
    private val coroutineDispatchers: CoroutineDispatchers by inject()
    private val stateManager: SwapStateManager
        get() = managerHolder.get(stateManagerKey)

    private val adapter = CommonAnyCellAdapter(
        baseCellDelegate(),
        swapInfoBannerDelegate(),
    )

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewRoutes.attachAdapter(adapter)
        binding.buttonDone.setOnClickListener { dismiss() }
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
                observeFeatureState()
            }
        }
        setExpanded(true)
    }

    private fun observeFeatureState() {
        stateManager.observe()
            .flatMapLatest { handleFeatureState(it) }
            .flowOn(coroutineDispatchers.io)
            .onEach { adapter.items = it }
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycleScope)
    }

    private suspend fun handleFeatureState(state: SwapState): Flow<List<AnyCellItem>> {
        return when (state) {
            SwapState.InitialLoading,
            is SwapState.LoadingRoutes,
            is SwapState.TokenANotZero,
            is SwapState.TokenAZero -> {
                flowOf(mapper.mapEmptyLiquidityFee())
            }
            is SwapState.SwapException -> {
                handleFeatureState(state.previousFeatureState)
            }
            is SwapState.RoutesLoaded,
            is SwapState.SwapLoaded -> {
                val route = when (state) {
                    is SwapState.RoutesLoaded -> state.route
                    is SwapState.SwapLoaded -> state.route
                    else -> return flowOf(mapper.mapEmptyLiquidityFee())
                }
                flow {
                    val rateLoaderList = mutableListOf<Flow<LoadRateBox>>()
                    val feeCells = route.routePlans.map { plan ->
                        val loadingCell = mapper.getLiquidityFeeCell(plan)
                        rateLoaderList += getRateLoaderFlow(plan, loadingCell)
                        loadingCell
                    }
                    var fullUiList = mapper.mapEmptyLiquidityFee().plus(feeCells)
                    emit(fullUiList)

                    rateLoaderList.merge()
                        .collect {
                            val routePlan = it.first
                            val loadingCell = it.second
                            val rateLoaderState = it.third
                            val indexOf = fullUiList.indexOf(loadingCell)
                            if (indexOf >= 0) {
                                val newCell = mapper.updateLiquidityFee(routePlan, loadingCell, rateLoaderState)
                                val newList = fullUiList.toMutableList()
                                    .apply { set(indexOf, newCell) }
                                fullUiList = newList
                                emit(newList)
                            }
                        }
                }
            }
            is SwapState.LoadingTransaction -> {
                flow {
                    val fullUiList = mapper.mapLoadingLiquidityFee(state.route)
                    emit(fullUiList)
                }
            }
        }
    }

    private suspend fun getRateLoaderFlow(
        routePlan: JupiterSwapRoutePlanV6,
        loadingCell: MainCellModel,
    ): Flow<LoadRateBox> {
        val lpToken = swapTokensRepository.findTokenByMint(routePlan.feeMint)
        val loadingCellFlow = flowOf(routePlan to loadingCell)
        val rateLoaderFlow = lpToken
            ?.let { stateManager.getTokenRate(SwapTokenModel.JupiterToken(it)) }
            ?: flowOf(SwapRateLoaderState.Error)
        return loadingCellFlow.combine(rateLoaderFlow) { a, b ->
            Triple(a.first, a.second, b)
        }
    }
}

fun swapInfoBannerDelegate(): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<SwapInfoBannerCellModel, AnyCellItem, ItemSwapInfoBannerBinding>(
        viewBinding = { inflater, parent -> ItemSwapInfoBannerBinding.inflate(inflater, parent, false) }
    ) {

        bind {
            binding.imageViewBanner.setImageResource(item.banner)
            binding.infoBlockView.bind(item.infoCell)
        }
    }

data class SwapInfoBannerCellModel(
    @DrawableRes val banner: Int,
    val infoCell: InfoBlockCellModel
) : AnyCellItem
