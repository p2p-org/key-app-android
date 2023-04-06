package org.p2p.wallet.history.ui.sendvialink

import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.applyBackground
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.image.bind
import org.p2p.uikit.utils.skeleton.SkeletonCellModel
import org.p2p.uikit.utils.skeleton.bindSkeleton
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bind
import org.p2p.uikit.utils.text.bindSkeleton
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogHistorySendLinkDetailsBinding
import org.p2p.wallet.history.analytics.HistoryAnalytics
import org.p2p.wallet.history.ui.sendvialink.HistorySendLinkDetailsContract.ViewState
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.shareText
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_LINK_UUID = "ARG_LINK_UUID"

class HistorySendLinkDetailsBottomSheet :
    BaseMvpBottomSheet<HistorySendLinkDetailsContract.View, HistorySendLinkDetailsContract.Presenter>(
        layoutRes = R.layout.dialog_history_send_link_details
    ),
    HistorySendLinkDetailsContract.View {

    companion object {
        fun show(fm: FragmentManager, linkUuid: String) {
            HistorySendLinkDetailsBottomSheet()
                .withArgs(ARG_LINK_UUID to linkUuid)
                .show(fm, HistorySendLinkDetailsBottomSheet::javaClass.name)
        }
    }

    private val linkUuid: String by args(ARG_LINK_UUID)
    private val binding: DialogHistorySendLinkDetailsBinding by viewBinding()

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override val presenter: HistorySendLinkDetailsContract.Presenter by inject { parametersOf(linkUuid) }
    private val historyAnalytics: HistoryAnalytics by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonDone.setOnClickListener { close() }

        DrawableCellModel(
            drawable = shapeDrawable(shapeCircle()),
            tint = R.color.bg_smoke
        ).applyBackground(binding.imageViewLinkDetails)
    }

    override fun renderState(state: ViewState) {
        when (state) {
            ViewState.Loading -> renderLoadingState()
            is ViewState.Content -> renderContent(state)
        }
    }

    private fun renderLoadingState() = with(binding) {
        textViewSubtitle.bindSkeleton(
            TextViewCellModel.Skeleton(SkeletonCellModel(height = 20.toPx(), width = 130.toPx(), radius = 10f.toPx()))
        )
        imageViewSecondIcon.bindSkeleton(
            SkeletonCellModel(height = 64.toPx(), width = 64.toPx(), radius = 64f.toPx())
        )
        textViewAmountUsd.bindSkeleton(
            TextViewCellModel.Skeleton(SkeletonCellModel(height = 32.toPx(), width = 212.toPx(), radius = 10f.toPx()))
        )

        textViewAmountTokens.bindSkeleton(
            TextViewCellModel.Skeleton(SkeletonCellModel(height = 32.toPx(), width = 170.toPx(), radius = 10f.toPx()))
        )

        val loadingLinkValue = TextViewCellModel.Skeleton(
            SkeletonCellModel(height = 12.toPx(), width = 212.toPx(), radius = 4f.toPx())
        )
        val loadingLeftSide = LeftSideCellModel.IconWithText(
            firstLineText = loadingLinkValue,
            secondLineText = TextViewCellModel.Raw(TextContainer(R.string.transaction_history_send_via_link_info))
        )
        financeBlockLinkValue.bind(
            FinanceBlockCellModel(
                leftSideCellModel = loadingLeftSide,
                rightSideCellModel = null
            )
        )
    }

    private fun renderContent(state: ViewState.Content) = with(binding) {
        val tokenIconContainer = state.iconUrl?.let(DrawableContainer::invoke)
            ?: DrawableContainer.invoke(R.drawable.ic_placeholder_image)

        imageViewSecondIcon.bind(ImageViewCellModel(icon = tokenIconContainer, clippingShape = shapeCircle()))
        textViewSubtitle.bind(TextViewCellModel.Raw(TextContainer(state.formattedDate)))
        textViewAmountUsd.bind(TextViewCellModel.Raw(TextContainer(state.formattedAmountUsd)))
        textViewAmountTokens.bind(TextViewCellModel.Raw(TextContainer(state.formattedTokenAmount)))

        val leftSideLinkValueContent = LeftSideCellModel.IconWithText(
            firstLineText = TextViewCellModel.Raw(TextContainer(state.link)),
            secondLineText = TextViewCellModel.Raw(TextContainer(R.string.transaction_history_send_via_link_info))
        )
        val rightSideCopyIcon = RightSideCellModel.IconWrapper(
            iconWrapper = IconWrapperCellModel.SingleIcon(
                ImageViewCellModel(DrawableContainer.invoke(R.drawable.ic_copy_filled_24))
            )
        )
        financeBlockLinkValue.bind(
            FinanceBlockCellModel(
                leftSideCellModel = leftSideLinkValueContent,
                rightSideCellModel = rightSideCopyIcon
            )
        )
        financeBlockLinkValue.rightSideView.setOnClickListener {
            historyAnalytics.logUserSendLinkCopyClicked()
            requireContext().copyToClipBoard(state.link)
            showUiKitSnackBar(messageResId = R.string.send_via_link_generation_copied)
        }
        buttonShare.setOnClickListener {
            historyAnalytics.logUserSendLinkShareClicked()
            requireContext().shareText(state.link)
        }
    }

    override fun close() {
        dismissAllowingStateLoss()
    }
}
