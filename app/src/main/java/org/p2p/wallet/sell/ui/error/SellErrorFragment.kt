package org.p2p.wallet.sell.ui.error

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSellErrorBinding
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_ERROR_STATE = "ARG_ERROR_STATE"

class SellErrorFragment : BaseFragment(R.layout.fragment_sell_error) {

    companion object {
        fun create(errorState: SellScreenError): SellErrorFragment =
            SellErrorFragment()
                .withArgs(ARG_ERROR_STATE to errorState)
    }

    private val binding: FragmentSellErrorBinding by viewBinding()
    private val sellErrorState: SellScreenError by args(ARG_ERROR_STATE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            textViewTitle.setText(sellErrorState.titleResId)
            textViewSubtitle.setText(sellErrorState.messageResId)
            imageView.setImageResource(sellErrorState.iconResId)
            buttonAction.setText(sellErrorState.buttonTextResId)

            buttonAction.setOnClickListener {
                when (sellErrorState) {
                    SellScreenError.SERVER_ERROR -> popBackStack()
                    SellScreenError.NOT_ENOUGH_AMOUNT -> replaceFragment(OrcaSwapFragment.create())
                }
            }
        }
    }

    enum class SellScreenError(
        @StringRes
        val titleResId: Int,
        @StringRes
        val messageResId: Int,
        @DrawableRes
        val iconResId: Int,
        val buttonTextResId: Int
    ) {
        SERVER_ERROR(
            titleResId = R.string.common_sorry,
            messageResId = R.string.error_service_ruiner_message,
            iconResId = R.drawable.ic_cat,
            buttonTextResId = R.string.common_go_back
        ),
        NOT_ENOUGH_AMOUNT(
            titleResId = R.string.sell_need_more_sol_title,
            messageResId = R.string.sell_need_more_sol_message,
            iconResId = R.drawable.ic_coins,
            buttonTextResId = R.string.common_go_to_swap
        )
    }
}
