package org.p2p.wallet.home.ui.topup

import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.natives.showSnackbarShort
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shape.shapeRounded16dp
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.viewState.ViewAccessibilityCellModel
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogTopupWalletBinding
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.receive.ReceiveFragmentFactory
import org.p2p.wallet.striga.StrigaFragmentFactory
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

class TopUpWalletBottomSheet :
    BaseMvpBottomSheet<TopUpWalletContract.View, TopUpWalletContract.Presenter>(R.layout.dialog_topup_wallet),
    TopUpWalletContract.View {

    companion object {
        fun show(fm: FragmentManager) {
            val tag = TopUpWalletBottomSheet::javaClass.name
            if (fm.findFragmentByTag(tag) != null) return
            TopUpWalletBottomSheet().show(fm, tag)
        }
    }

    private val binding: DialogTopupWalletBinding by viewBinding()
    private val receiveFragmentFactory: ReceiveFragmentFactory by inject()
    private val strigaFragmentFactory: StrigaFragmentFactory by inject()

    override val presenter: TopUpWalletContract.Presenter by inject()

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override fun showStrigaBankTransferView(showProgress: Boolean) {
        binding.bankTransferView.isVisible = true
        binding.bankTransferView.bind(
            model = getFinanceBlock(
                titleResId = R.string.bank_transfer_title,
                subtitleRes = R.string.bank_transfer_subtitle,
                iconResId = R.drawable.ic_bank_transfer,
                backgroundTintId = R.color.light_grass,
                showRightProgress = showProgress
            )
        )
        binding.bankTransferView.setOnClickAction { _, _ ->
            if (showProgress) return@setOnClickAction
            presenter.onBankTransferClicked()
        }
    }

    override fun hideStrigaBankTransferView() {
        binding.bankTransferView.isVisible = false
    }

    override fun navigateToBankTransferTarget(target: StrigaUserStatusDestination) {
        strigaFragmentFactory.signupFlowFragment(target)?.let(::dismissAndNavigate)
    }

    override fun navigateToBuyWithTransfer(tokenToBuy: Token) {
        dismissAndNavigate(NewBuyFragment.create(tokenToBuy))
    }

    override fun navigateToKycPending() {
        dismiss()
        strigaFragmentFactory.showPendingBottomSheet(requireActivity().supportFragmentManager)
    }

    override fun showBankCardView(tokenToBuy: Token) {
        binding.bankCardView.isVisible = true
        binding.bankCardView.bind(
            model = getFinanceBlock(
                titleResId = R.string.bank_card_title,
                subtitleRes = R.string.bank_card_subtitle,
                iconResId = R.drawable.ic_bank_card,
                backgroundTintId = R.color.light_sea
            )
        )
        binding.bankCardView.setOnClickAction { _, _ ->
            dismissAndNavigate(
                NewBuyFragment.create(
                    token = tokenToBuy,
                    preselectedMethodType = PaymentMethod.MethodType.CARD
                )
            )
        }
    }

    override fun hideBankCardView() {
        binding.bankCardView.isVisible = false
    }

    override fun showCryptoReceiveView() {
        binding.cryptoView.bind(
            model = getFinanceBlock(
                titleResId = R.string.crypto_title,
                subtitleRes = R.string.crypto_subtitle,
                iconResId = R.drawable.ic_crypto,
                backgroundTintId = R.color.light_sun
            )
        )
        binding.cryptoView.setOnClickAction { _, _ ->
            dismissAndNavigate(receiveFragmentFactory.receiveFragment())
        }
    }

    override fun showUiKitSnackBar(
        message: String?,
        messageResId: Int?,
        onDismissed: () -> Unit,
        actionButtonResId: Int?,
        actionBlock: ((Snackbar) -> Unit)?
    ) {
        require(message != null || messageResId != null) {
            "Snackbar text must be set from `message` or `messageResId` params"
        }
        val snackbarText: String = message ?: messageResId?.let(::getString)!!
        val root = requireView().rootView
        if (actionButtonResId != null && actionBlock != null) {
            root.showSnackbarShort(
                snackbarText = snackbarText,
                actionButtonText = getString(actionButtonResId),
                actionButtonListener = actionBlock,
                enableBottomNavOffset = false
            )
        } else {
            root.showSnackbarShort(
                snackbarText = snackbarText,
                onDismissed = onDismissed,
                enableBottomNavOffset = false
            )
        }
    }

    private fun getFinanceBlock(
        titleResId: Int,
        subtitleRes: Int,
        iconResId: Int,
        backgroundTintId: Int,
        showRightProgress: Boolean = false
    ): MainCellModel {
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            icon = IconWrapperCellModel.SingleIcon(
                icon = ImageViewCellModel(
                    icon = DrawableContainer(iconResId),
                    background = DrawableCellModel(tint = backgroundTintId),
                    clippingShape = shapeCircle()
                )
            ),
            firstLineText = TextViewCellModel.Raw(
                text = TextContainer(titleResId),
                textAppearance = R.style.UiKit_TextAppearance_SemiBold_Text3,
                textColor = R.color.text_night
            ),
            secondLineText = TextViewCellModel.Raw(
                text = TextContainer(subtitleRes),
                textAppearance = R.style.UiKit_TextAppearance_Regular_Label1,
                textColor = R.color.text_mountain
            )
        )

        val rightSideCellModel = if (showRightProgress) {
            RightSideCellModel.Progress(
                indeterminateProgressTint = R.color.night
            )
        } else {
            RightSideCellModel.IconWrapper(
                iconWrapper = IconWrapperCellModel.SingleIcon(
                    icon = ImageViewCellModel(
                        icon = DrawableContainer(R.drawable.ic_chevron_right),
                        iconTint = R.color.icons_mountain
                    )
                )
            )
        }
        val background = DrawableCellModel(
            drawable = shapeDrawable(shapeRounded16dp()),
            tint = R.color.bg_snow
        )

        return MainCellModel(
            leftSideCellModel = leftSideCellModel,
            rightSideCellModel = rightSideCellModel,
            background = background,
            accessibility = ViewAccessibilityCellModel(isClickable = true),
        )
    }

    private fun dismissAndNavigate(fragment: Fragment) {
        replaceFragment(fragment)
        dismiss()
    }
}
