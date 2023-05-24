package org.p2p.wallet.striga.ui

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import kotlinx.coroutines.launch
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
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
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.common.ui.bottomsheet.BaseBottomSheet
import org.p2p.wallet.databinding.DialogTopupWalletBinding
import org.p2p.wallet.moonpay.ui.BuySolanaFragment
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.receive.ReceiveFragmentFactory
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

class TopUpWalletBottomSheet : BaseBottomSheet(R.layout.dialog_topup_wallet) {

    companion object {
        fun show(token: Token.Active? = null, fm: FragmentManager) {
            val tag = TopUpWalletBottomSheet::javaClass.name
            if (fm.findFragmentByTag(tag) != null) return
            TopUpWalletBottomSheet().show(fm, tag)
        }
    }

    private val binding: DialogTopupWalletBinding by viewBinding()
    private val receiveFragmentFactory: ReceiveFragmentFactory by inject()
    private val token: Token.Active? by args(EXTRA_TOKEN)
    private val newBuyFeatureToggle: NewBuyFeatureToggle by inject()
    private val userInteractor: UserInteractor by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            bankTransferView.bind(
                model = getFinanceBlock(
                    titleResId = R.string.bank_transfer_title,
                    subtitleRes = R.string.bank_transfer_subtitle,
                    iconResId = R.drawable.ic_bank_transfer,
                    backgroundTintId = R.color.light_grass
                )
            )
            bankTransferView.setOnClickAction { _, _ ->
                // TODO PWN-8457
            }
            bankCardView.bind(
                model = getFinanceBlock(
                    titleResId = R.string.bank_card_title,
                    subtitleRes = R.string.bank_card_subtitle,
                    iconResId = R.drawable.ic_bank_card,
                    backgroundTintId = R.color.light_sea
                )
            )
            bankCardView.setOnClickAction { _, _ ->
                lifecycleScope.launch {
                    val tokenForBuy = token ?: userInteractor.getTokensForBuy().firstOrNull() ?: return@launch
                    if (newBuyFeatureToggle.isFeatureEnabled) {
                        replaceFragment(NewBuyFragment.create(tokenForBuy))
                    } else {
                        replaceFragment(BuySolanaFragment.create(tokenForBuy))
                    }
                }
            }
            cryptoView.bind(
                model = getFinanceBlock(
                    titleResId = R.string.crypto_title,
                    subtitleRes = R.string.crypto_subtitle,
                    iconResId = R.drawable.ic_crypto,
                    backgroundTintId = R.color.light_sun
                )
            )
            cryptoView.setOnClickAction { _, _ ->
                replaceFragment(receiveFragmentFactory.receiveFragment())
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
