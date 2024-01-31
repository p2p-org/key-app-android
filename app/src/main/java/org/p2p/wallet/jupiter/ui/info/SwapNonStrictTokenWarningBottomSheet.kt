package org.p2p.wallet.jupiter.ui.info

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import android.os.Bundle
import android.view.View
import org.p2p.core.common.DrawableContainer
import org.p2p.core.common.TextContainer
import org.p2p.core.crypto.Base58String
import org.p2p.uikit.components.icon_wrapper.IconWrapperCellModel
import org.p2p.uikit.components.info_block.InfoBlockCellModel
import org.p2p.uikit.utils.drawable.DrawableCellModel
import org.p2p.uikit.utils.drawable.shape.shapeCircle
import org.p2p.uikit.utils.drawable.shape.shapeRoundedAll
import org.p2p.uikit.utils.drawable.shapeDrawable
import org.p2p.uikit.utils.image.ImageViewCellModel
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.common.ui.bottomsheet.BaseBottomSheet
import org.p2p.wallet.databinding.DialogSwapNonStrictWarningBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class SwapNonStrictTokenWarningBottomSheet : BaseBottomSheet(R.layout.dialog_swap_non_strict_warning) {

    companion object {
        private const val ARG_TOKEN_SYMBOL = "ARG_TOKEN_SYMBOL"
        private const val ARG_TOKEN_MINT = "ARG_TOKEN_MINT"
        const val KEY_RESULT_CONFIRMED_TOKEN = "KEY_RESULT_CONFIRMED_TOKEN"
        const val KEY_REQUEST = "KEY_REQUEST"

        fun show(
            fm: FragmentManager,
            selectedTokenSymbol: String,
            selectedTokenMint: Base58String
        ) {
            val tag = SwapNonStrictTokenWarningBottomSheet::javaClass.name
            if (fm.findFragmentByTag(tag) != null) return

            SwapNonStrictTokenWarningBottomSheet()
                .withArgs(
                    ARG_TOKEN_SYMBOL to selectedTokenSymbol,
                    ARG_TOKEN_MINT to selectedTokenMint.base58Value
                )
                .show(fm, tag)
        }
    }

    private val binding: DialogSwapNonStrictWarningBinding by viewBinding()

    private val tokenSymbol: String by args(ARG_TOKEN_SYMBOL)
    private val tokenMint: String by args(ARG_TOKEN_MINT)

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_RoundedSnow

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val icon = ImageViewCellModel(
            icon = DrawableContainer(R.drawable.ic_info_rounded),
            background = DrawableCellModel(tint = R.color.bg_snow),
            clippingShape = shapeCircle(),
        ).let(IconWrapperCellModel::SingleIcon)

        val title = TextViewCellModel.Raw(
            text = TextContainer(
                R.string.swap_non_strict_warning_title,
                tokenSymbol
            ),
            textColor = R.color.text_night
        )
        val body = TextViewCellModel.Raw(
            text = TextContainer(
                R.string.swap_non_strict_warning_body,
                tokenSymbol,
                tokenMint
            ),
            textColor = R.color.text_night
        )

        val background = DrawableCellModel(
            drawable = shapeDrawable(shapeRoundedAll(12f.toPx())),
            tint = R.color.bg_smoke
        )

        binding.infoBlock.bind(
            InfoBlockCellModel(
                icon = icon,
                firstLineText = title,
                secondLineText = body,
                background = background
            )
        )
        binding.buttonConfirm.setOnClickListener {
            setFragmentResult(
                requestKey = KEY_REQUEST,
                result = bundleOf(KEY_RESULT_CONFIRMED_TOKEN to tokenMint)
            )
            dismiss()
        }
    }
}
