package org.p2p.wallet.striga.ui

import androidx.fragment.app.FragmentManager
import android.os.Bundle
import android.view.View
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shape.shapeRounded16dp
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.viewState.ViewAccessibilityCellModel
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.bottomsheet.BaseBottomSheet
import org.p2p.wallet.databinding.DialogStrigaOnRampBinding
import org.p2p.wallet.utils.viewbinding.viewBinding

class StrigaOnRampBottomSheet : BaseBottomSheet(R.layout.dialog_striga_on_ramp) {

    companion object {
        fun show(fm: FragmentManager) {
            val tag = StrigaOnRampBottomSheet::javaClass.name
            if (fm.findFragmentByTag(tag) != null) return
            StrigaOnRampBottomSheet().show(fm, tag)
        }
    }

    private val binding: DialogStrigaOnRampBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            bankTransferView.bind(
                model = getFinanceBlock(
                    titleResId = R.string.striga_bank_transfer_title,
                    subtitleRes = R.string.striga_bank_transfer_subtitle,
                    iconResId = R.drawable.ic_striga_bank_transfer,
                    backgroundTintId = R.color.light_grass
                )
            )
            bankTransferView.setOnClickAction { _, _ ->
                // TODO PWN-8457
            }
            bankCardView.bind(
                model = getFinanceBlock(
                    titleResId = R.string.striga_bank_card_title,
                    subtitleRes = R.string.striga_bank_card_subtitle,
                    iconResId = R.drawable.ic_striga_bank_card,
                    backgroundTintId = R.color.light_sea
                )
            )
            bankCardView.setOnClickAction { _, _ ->
                // TODO PWN-8457
            }
            cryptoView.bind(
                model = getFinanceBlock(
                    titleResId = R.string.striga_crypto_title,
                    subtitleRes = R.string.striga_crypto_subtitle,
                    iconResId = R.drawable.ic_striga_crypto,
                    backgroundTintId = R.color.light_sun
                )
            )
            cryptoView.setOnClickAction { _, _ ->
                // TODO PWN-8457
            }
        }
    }

    private fun getFinanceBlock(
        titleResId: Int,
        subtitleRes: Int,
        iconResId: Int,
        backgroundTintId: Int
    ): FinanceBlockCellModel {
        val leftSideCellModel = LeftSideCellModel.IconWithText(
            icon = IconWrapperCellModel.SingleIcon(
                icon = ImageViewCellModel(
                    icon = DrawableContainer(iconResId),
                    background = DrawableCellModel(
                        tint = backgroundTintId
                    ),
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

        val rightSideCellModel = RightSideCellModel.IconWrapper(
            iconWrapper = IconWrapperCellModel.SingleIcon(
                icon = ImageViewCellModel(
                    icon = DrawableContainer(R.drawable.ic_chevron_right),
                    iconTint = R.color.icons_mountain
                )
            )
        )

        return FinanceBlockCellModel(
            leftSideCellModel = leftSideCellModel,
            rightSideCellModel = rightSideCellModel,
            background = DrawableCellModel(drawable = shapeDrawable(shapeRounded16dp()), tint = R.color.bg_snow),
            accessibility = ViewAccessibilityCellModel(isClickable = true),
        )
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow
}
