package org.p2p.wallet.receive.tokenselect.dialog

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.finance_block.financeBlockCellDelegate
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.image.commonCircleImage
import org.p2p.uikit.utils.recycler.decoration.offsetFinanceBlockDecoration
import org.p2p.uikit.utils.recycler.decoration.roundingByCellFinanceBlockDecoration
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.ui.bottomsheet.BaseRecyclerDoneBottomSheet
import org.p2p.wallet.receive.tokenselect.models.ReceiveNetwork
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.getColorStateListCompat
import org.p2p.wallet.utils.withArgs

const val ARG_NETWORKS_TOKENS = "ARG_NETWORKS_TOKENS"

class SelectReceiveNetworkBottomSheet : BaseRecyclerDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            title: String,
            requestKey: String,
            resultKey: String,
            tokensForNetworks: List<Token>
        ) =
            SelectReceiveNetworkBottomSheet()
                .withArgs(
                    ARG_TITLE to title,
                    ARG_NETWORKS_TOKENS to tokensForNetworks,
                    ARG_REQUEST_KEY to requestKey,
                    ARG_RESULT_KEY to resultKey
                )
                .show(fm, SelectReceiveNetworkBottomSheet::javaClass.name)
    }

    private val tokenDataForNetworks: List<Token> by args(ARG_NETWORKS_TOKENS)

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSmoke

    private val networkAdapter = CommonAnyCellAdapter(
        financeBlockCellDelegate(inflateListener = { financeBlock ->
            financeBlock.setOnClickAction { _, item -> onNetworkClick(item) }
        })
    )

    private fun onNetworkClick(item: FinanceBlockCellModel) {
        val network = item.typedPayload<ReceiveNetwork>()
        setFragmentResult(requestKey, bundleOf(resultKey to network))
        dismissAllowingStateLoss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseDialogBinding.root.backgroundTintList = requireContext().getColorStateListCompat(R.color.bg_smoke)
        setDoneButtonVisibility(isVisible = false)
        with(recyclerBinding.recyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            attachAdapter(networkAdapter)
            addItemDecoration(roundingByCellFinanceBlockDecoration())
            addItemDecoration(offsetFinanceBlockDecoration())
            networkAdapter.items = makeNetworkDataToSelect()
        }
    }

    override fun getResult(): Any? = null

    private fun makeNetworkDataToSelect(): List<AnyCellItem> {
        return listOf<AnyCellItem>(
            createUiCellModel(
                ReceiveNetwork.SOLANA,
                tokenDataForNetworks.firstOrNull { it.tokenSymbol == Constants.SOL_SYMBOL }
            ),
            createUiCellModel(
                ReceiveNetwork.ETHEREUM,
                tokenDataForNetworks.firstOrNull { it.tokenSymbol == Constants.ETH_SYMBOL }
            )
        )
    }

    private fun createUiCellModel(network: ReceiveNetwork, token: Token?): FinanceBlockCellModel {
        return FinanceBlockCellModel(
            leftSideCellModel = createLeftSideModel(
                tokenIconUrl = token?.iconUrl.orEmpty(),
                tokenName = token?.tokenName.orEmpty(),
            ),
            payload = network
        )
    }

    private fun createLeftSideModel(
        tokenIconUrl: String,
        tokenName: String
    ): LeftSideCellModel.IconWithText {
        val tokenIconImage =
            DrawableContainer.Raw(iconUrl = tokenIconUrl)
                .let(::commonCircleImage)
                .let(IconWrapperCellModel::SingleIcon)

        val firstLineText = TextViewCellModel.Raw(TextContainer.Raw(tokenName))

        return LeftSideCellModel.IconWithText(
            icon = tokenIconImage,
            firstLineText = firstLineText
        )
    }
}
