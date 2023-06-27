package org.p2p.wallet.striga.iban

import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.left_side.LeftSideCellModel
import org.p2p.uikit.components.right_side.RightSideCellModel
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter

class StrigaUserIbanDetailsPresenter :
    BasePresenter<StrigaUserIbanDetailsContract.View>(),
    StrigaUserIbanDetailsContract.Presenter {
    override fun attach(view: StrigaUserIbanDetailsContract.View) {
        super.attach(view)

        val iconModel = IconWrapperCellModel.SingleIcon(
            ImageViewCellModel(
                DrawableContainer.invoke(R.drawable.ic_copy_outlined),
                iconTint = R.color.icons_mountain
            )
        )
        view.showIbanDetails(
            listOf(
                MainCellModel(
                    leftSideCellModel = LeftSideCellModel.IconWithText(
                        firstLineText = TextViewCellModel.Raw(
                            TextContainer.invoke("IBAN")
                        ),
                        secondLineText = TextViewCellModel.Raw(
                            TextContainer.invoke("AD42 5634 6435 4324 5325")
                        )
                    ),
                    rightSideCellModel = RightSideCellModel.IconWrapper(iconModel),
                    payload = "AD42 5634 6435 4324 5325"
                ),
                MainCellModel(
                    leftSideCellModel = LeftSideCellModel.IconWithText(
                        firstLineText = TextViewCellModel.Raw(
                            TextContainer.invoke("Currency")
                        ),
                        secondLineText = TextViewCellModel.Raw(
                            TextContainer.invoke("EUR")
                        )
                    ),
                    rightSideCellModel = null,
                ),
                MainCellModel(
                    leftSideCellModel = LeftSideCellModel.IconWithText(
                        firstLineText = TextViewCellModel.Raw(
                            TextContainer.invoke("BIC")
                        ),
                        secondLineText = TextViewCellModel.Raw(
                            TextContainer.invoke("CODEWORD")
                        )
                    ),
                    rightSideCellModel = RightSideCellModel.IconWrapper(iconModel),
                    payload = "CODEWORD"
                ),
                MainCellModel(
                    leftSideCellModel = LeftSideCellModel.IconWithText(
                        firstLineText = TextViewCellModel.Raw(
                            TextContainer.invoke("Beneficiary")
                        ),
                        secondLineText = TextViewCellModel.Raw(
                            TextContainer.invoke("Name Surmane")
                        )
                    ),
                    rightSideCellModel = RightSideCellModel.IconWrapper(iconModel),
                    payload = "Name Surmane"
                ),
            )
        )
    }
}
