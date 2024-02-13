package org.p2p.wallet.jupiter.ui.info

import androidx.annotation.DrawableRes
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.view.View
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.uikit.components.finance_block.baseCellDelegate
import org.p2p.uikit.components.info_block.InfoBlockCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.ui.bottomsheet.BaseBottomSheet
import org.p2p.wallet.databinding.DialogSwapInfoBinding
import org.p2p.wallet.databinding.ItemSwapInfoBannerBinding
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.jupiter.statemanager.SwapStateManagerHolder
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_STATE_MANAGE_KEY = "ARG_STATE_MANAGE_KEY"
private const val ARG_INFO_TYPE_KEY = "ARG_INFO_TYPE_KEY"

enum class SwapInfoType {
    NETWORK_FEE,
    ACCOUNT_FEE,
    LIQUIDITY_FEE,
    MINIMUM_RECEIVED,
    TOKEN_2022_INTEREST,
    TOKEN_2022_TRANSFER
}

private fun swapInfoBannerDelegate(): AdapterDelegate<List<AnyCellItem>> =
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

class SwapFeeInfoBottomSheet : BaseBottomSheet(R.layout.dialog_swap_info) {

    companion object {
        fun show(
            fm: FragmentManager,
            stateManagerKey: String,
            swapInfoType: SwapInfoType,
        ) {
            val tag = SwapFeeInfoBottomSheet::javaClass.name
            if (fm.findFragmentByTag(tag) != null) return
            SwapFeeInfoBottomSheet()
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
    private val mapper = SwapInfoMapper()
    private val liquidityFeeMapper: SwapInfoLiquidityFeeMapper by lazy {
        SwapInfoLiquidityFeeMapper(swapTokensRepository = get(), tokenServiceRepository = get())
    }

    private val dispatchers: CoroutineDispatchers by inject()
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
            SwapInfoType.TOKEN_2022_INTEREST, SwapInfoType.TOKEN_2022_TRANSFER -> {
                lifecycleScope.launch {
                    showToken2022Fees()
                    binding.buttonDone.setText(R.string.swap_info_details_account_fee_done)
                }
            }
        }
        setExpanded(true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeFeatureState() {
        stateManager.observe()
            .flatMapLatest(transform = ::mapLiquidityCellsFlow)
            .flowOn(dispatchers.io)
            .onEach(adapter::setItems)
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycleScope)
    }

    private fun mapLiquidityCellsFlow(state: SwapState): Flow<List<AnyCellItem>> {
        return when (state) {
            SwapState.InitialLoading,
            is SwapState.LoadingRoutes,
            is SwapState.TokenANotZero,
            is SwapState.TokenAZero -> {
                flowOf(liquidityFeeMapper.mapNoRouteLoaded())
            }
            is SwapState.SwapException -> {
                mapLiquidityCellsFlow(state.previousFeatureState)
            }
            is SwapState.RoutesLoaded,
            is SwapState.LoadingTransaction,
            is SwapState.SwapLoaded -> {
                val route = when (state) {
                    is SwapState.RoutesLoaded -> state.route
                    is SwapState.SwapLoaded -> state.route
                    is SwapState.LoadingTransaction -> state.route
                    else -> null
                } ?: return flowOf(liquidityFeeMapper.mapNoRouteLoaded())

                liquidityFeeMapper.mapLiquidityFees(route)
            }
        }
    }

    private fun showToken2022Fees() {
        adapter.items = mapper.mapToken2022Fee(
            isTransferFee = swapInfoType == SwapInfoType.TOKEN_2022_TRANSFER,
        )
    }
}
