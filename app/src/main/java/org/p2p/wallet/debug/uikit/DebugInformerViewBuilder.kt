package org.p2p.wallet.debug.uikit

import androidx.annotation.DrawableRes
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.uikit.components.InformerViewCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.wallet.R

class DebugInformerViewBuilder {
    fun build(
        onInfoLineClicked: () -> Unit
    ): List<AnyCellItem> = listOf(
        SectionHeaderCellModel(
            sectionTitle = TextContainer("Informer view row 1"),
            isShevronVisible = false
        ),
        InformerViewCellModel(
            leftIcon = InformerViewCellModel.LeftIconParams(
                icon = R.drawable.ic_checkbox_checked.wrap(),
                iconTint = R.color.icons_night
            ),
            title = InformerViewCellModel.TitleParams(
                value = "Account creation fee".wrap(),
                titleIcon = R.drawable.ic_info_solid.wrap()
            ),
            caption = "0.028813 USDC".wrap(),
            infoLine = InformerViewCellModel.InfoLineParams(
                value = "Info line".wrap(),
                position = InformerViewCellModel.InfoLineParams.InfoLinePosition.BOTTOM,
                onInfoLineClicked = { onInfoLineClicked() }
            ),
        ),
        InformerViewCellModel(
            leftIcon = InformerViewCellModel.LeftIconParams(
                R.drawable.ic_checkbox_checked.wrap(),
                iconTint = R.color.icons_night
            ),
            title = InformerViewCellModel.TitleParams(
                "Account creation fee".wrap(),
                R.drawable.ic_info_solid.wrap()
            ),
            caption = "0.028813 USDC".wrap(),
            infoLine = InformerViewCellModel.InfoLineParams(
                value = "(\$0.03)".wrap(),
                position = InformerViewCellModel.InfoLineParams.InfoLinePosition.CAPTION_LINE,
                textColorRes = R.color.text_sky,
                onInfoLineClicked = { onInfoLineClicked() }
            )
        ),
        InformerViewCellModel(
            leftIcon = InformerViewCellModel.LeftIconParams(
                R.drawable.ic_checkbox_checked.wrap(),
                iconTint = R.color.icons_night
            ),
            title = InformerViewCellModel.TitleParams(
                "Account creation fee".wrap(),
                R.drawable.ic_info_solid.wrap()
            ),
            caption = "0.028813 USDC".wrap(),
        )
    ) +

        listOf(
            SectionHeaderCellModel(
                sectionTitle = TextContainer("Informer view row 2"),
                isShevronVisible = false
            ),
            InformerViewCellModel(
                leftIcon = InformerViewCellModel.LeftIconParams(
                    icon = R.drawable.ic_checkbox_checked.wrap(),
                    iconTint = R.color.icons_night
                ),
                title = InformerViewCellModel.TitleParams(
                    value = "Account creation fee".wrap(),
                ),
                caption = "0.028813 USDC".wrap(),
                infoLine = InformerViewCellModel.InfoLineParams(
                    value = "Info line".wrap(),
                    position = InformerViewCellModel.InfoLineParams.InfoLinePosition.BOTTOM,
                    onInfoLineClicked = { onInfoLineClicked() }
                ),
            ),
            InformerViewCellModel(
                leftIcon = InformerViewCellModel.LeftIconParams(
                    icon = R.drawable.ic_checkbox_checked.wrap(),
                    iconTint = R.color.icons_night
                ),
                title = InformerViewCellModel.TitleParams(
                    value = "Account creation fee".wrap(),
                ),
                caption = "0.028813 USDC".wrap(),
                infoLine = InformerViewCellModel.InfoLineParams(
                    value = "(\$0.03)".wrap(),
                    position = InformerViewCellModel.InfoLineParams.InfoLinePosition.CAPTION_LINE,
                    textColorRes = R.color.text_sky,
                    onInfoLineClicked = { onInfoLineClicked() }
                )
            ),
            InformerViewCellModel(
                leftIcon = InformerViewCellModel.LeftIconParams(
                    icon = R.drawable.ic_checkbox_checked.wrap(),
                    iconTint = R.color.icons_night
                ),
                title = InformerViewCellModel.TitleParams(
                    value = "Account creation fee".wrap(),
                ),
                caption = "0.028813 USDC".wrap(),
            ),
        ) +

        listOf(
            SectionHeaderCellModel(
                sectionTitle = TextContainer("Informer view row 3"),
                isShevronVisible = false
            ),
            InformerViewCellModel(
                leftIcon = InformerViewCellModel.LeftIconParams(
                    icon = R.drawable.ic_checkbox_checked.wrap(),
                    iconTint = R.color.icons_night
                ),
                caption = "0.028813 USDC".wrap(),
                infoLine = InformerViewCellModel.InfoLineParams(
                    value = "Info line".wrap(),
                    position = InformerViewCellModel.InfoLineParams.InfoLinePosition.BOTTOM,
                    onInfoLineClicked = { onInfoLineClicked() }
                ),
            ),
            InformerViewCellModel(
                leftIcon = InformerViewCellModel.LeftIconParams(
                    icon = R.drawable.ic_checkbox_checked.wrap(),
                    iconTint = R.color.icons_night
                ),
                caption = "The minimum amount you will receive. If the price slips any further ".wrap(),
                infoLine = InformerViewCellModel.InfoLineParams(
                    value = "(\$0.03)".wrap(),
                    position = InformerViewCellModel.InfoLineParams.InfoLinePosition.CAPTION_LINE,
                    textColorRes = R.color.text_sky,
                    onInfoLineClicked = { onInfoLineClicked() }
                )
            ),
            InformerViewCellModel(
                leftIcon = InformerViewCellModel.LeftIconParams(
                    icon = R.drawable.ic_checkbox_checked.wrap(),
                    iconTint = R.color.icons_night
                ),
                caption = buildString {
                    append("The minimum amount you will receive. ")
                    append("If the price slips any further, your transaction will revert.")
                }.wrap(),
            ),
        )

    private fun @receiver:DrawableRes Int.wrap(): DrawableContainer.Res =
        DrawableContainer.invoke(this)

    private fun String.wrap(): TextContainer.Raw =
        TextContainer.Raw(this)
}
