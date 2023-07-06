package org.p2p.wallet.receive.tokenselect.dialog

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.Constants
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.mainCellDelegate
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.image.commonCircleImage
import org.p2p.uikit.utils.recycler.decoration.offsetFinanceBlockDecoration
import org.p2p.uikit.utils.recycler.decoration.roundingByCellMainCellDecoration
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.common.adapter.CommonAnyCellAdapter
import org.p2p.wallet.common.ui.bottomsheet.BaseRecyclerDoneBottomSheet
import org.p2p.wallet.receive.tokenselect.models.ReceiveNetwork
import org.p2p.wallet.utils.getColorStateListCompat
import org.p2p.wallet.utils.withArgs

class SelectReceiveNetworkBottomSheet : BaseRecyclerDoneBottomSheet() {

    companion object {
        fun show(
            fm: FragmentManager,
            title: String,
            requestKey: String,
            resultKey: String,
        ) =
            SelectReceiveNetworkBottomSheet()
                .withArgs(
                    ARG_TITLE to title,
                    ARG_REQUEST_KEY to requestKey,
                    ARG_RESULT_KEY to resultKey
                )
                .show(fm, SelectReceiveNetworkBottomSheet::javaClass.name)
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSmoke

    private val networkAdapter = CommonAnyCellAdapter(
        mainCellDelegate(inflateListener = { financeBlock ->
            financeBlock.setOnClickAction { _, item -> onNetworkClick(item) }
        })
    )

    private fun onNetworkClick(item: MainCellModel) {
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
            addItemDecoration(roundingByCellMainCellDecoration())
            addItemDecoration(offsetFinanceBlockDecoration())
            networkAdapter.items = makeNetworkDataToSelect()
        }
    }

    override fun getResult(): Any? = null

    private fun makeNetworkDataToSelect(): List<AnyCellItem> {
        return listOf<AnyCellItem>(
            createUiCellModel(
                ReceiveNetwork.SOLANA,
                Constants.SOL_NAME,
                ERC20Tokens.SOL_TOKEN_URL
            ),
            createUiCellModel(
                ReceiveNetwork.ETHEREUM,
                Constants.ETH_NAME,
                ERC20Tokens.ETH.tokenIconUrl.orEmpty()
            )
        )
    }

    private fun createUiCellModel(
        network: ReceiveNetwork,
        networkName: String,
        tokenIcon: String
    ): MainCellModel {
        return MainCellModel(
            leftSideCellModel = createLeftSideModel(
                tokenIconUrl = tokenIcon,
                tokenName = networkName,
            ),
            rightSideCellModel = RightSideCellModel.SingleTextTwoIcon(
                firstIcon = ImageViewCellModel(
                    icon = DrawableContainer(R.drawable.ic_chevron_right),
                    iconTint = R.color.icons_mountain,
                )
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
