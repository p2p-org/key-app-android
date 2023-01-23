package org.p2p.wallet.sell.ui.error

import android.content.res.Resources
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSellErrorBinding
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.sell.analytics.SellAnalytics
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStackTo
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import kotlinx.parcelize.Parcelize

private const val ARG_ERROR_STATE = "ARG_ERROR_STATE"

class SellErrorFragment : BaseFragment(R.layout.fragment_sell_error) {

    companion object {
        fun create(errorState: SellScreenError): SellErrorFragment =
            SellErrorFragment()
                .withArgs(ARG_ERROR_STATE to errorState)
    }

    private val binding: FragmentSellErrorBinding by viewBinding()
    private val sellErrorState: SellScreenError by args(ARG_ERROR_STATE)
    private val sellAnalytics: SellAnalytics by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        logScreenOpened()
    }

    private fun setupView() = with(binding) {
        toolbar.setNavigationOnClickListener { popBackStackTo(MainFragment::class) }
        textViewTitle.setText(sellErrorState.titleResId)
        imageView.setImageResource(sellErrorState.iconResId)
        buttonAction.setText(sellErrorState.buttonTextResId)

        textViewSubtitle.text = sellErrorState.getSubtitle(resources)

        buttonAction.setOnClickListener {
            when (sellErrorState) {
                is SellScreenError.ServerError -> {
                    popBackStackTo(MainFragment::class)
                }
                is SellScreenError.NotEnoughAmount -> {
                    sellAnalytics.logSellErrorMinAmountSwapClicked()
                    replaceFragment(OrcaSwapFragment.create())
                }
            }
        }
    }

    private fun logScreenOpened() {
        when (sellErrorState) {
            is SellScreenError.ServerError -> sellAnalytics.logSellServerErrorOpened()
            is SellScreenError.NotEnoughAmount -> sellAnalytics.logSellErrorMinAmountOpened()
        }
    }

    @Parcelize
    sealed class SellScreenError : Parcelable {
        abstract val titleResId: Int
        abstract val iconResId: Int
        abstract val buttonTextResId: Int

        abstract fun getSubtitle(resources: Resources): String

        @Parcelize
        data class ServerError(
            override val titleResId: Int = R.string.common_sorry,
            override val iconResId: Int = R.drawable.ic_cat,
            override val buttonTextResId: Int = R.string.common_go_back
        ) : SellScreenError() {
            override fun getSubtitle(resources: Resources): String =
                resources.getString(R.string.sell_error_body_message)
        }

        @Parcelize
        data class NotEnoughAmount(
            val formattedMinTokenAmount: String,
            override val titleResId: Int = R.string.sell_error_min_amount_title,
            override val iconResId: Int = R.drawable.ic_coins,
            override val buttonTextResId: Int = R.string.common_go_to_swap,
        ) : SellScreenError() {
            override fun getSubtitle(resources: Resources): String =
                resources.getString(R.string.sell_error_min_amount_subtitle, formattedMinTokenAmount)
        }
    }
}
