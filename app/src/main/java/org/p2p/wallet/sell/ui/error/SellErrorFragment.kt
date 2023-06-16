package org.p2p.wallet.sell.ui.error

import android.content.res.Resources
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.utils.insets.appleBottomInsets
import org.p2p.core.utils.insets.appleTopInsets
import org.p2p.core.utils.insets.consume
import org.p2p.core.utils.insets.doOnApplyWindowInsets
import org.p2p.core.utils.insets.systemAndIme
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseFragment
import org.p2p.wallet.databinding.FragmentSellErrorBinding
import org.p2p.wallet.home.ui.container.MainContainerFragment
import org.p2p.wallet.sell.analytics.SellAnalytics
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStackTo
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

    override fun applyWindowInsets(rootView: View) {
        rootView.doOnApplyWindowInsets { _, insets, _ ->
            insets.systemAndIme().consume {
                binding.toolbar.appleTopInsets(this)
                rootView.appleBottomInsets(this)
            }
        }
    }

    private fun setupView() = with(binding) {
        toolbar.setNavigationOnClickListener { popBackStackTo(MainContainerFragment::class) }
        textViewTitle.setText(sellErrorState.titleResId)
        imageView.setImageResource(sellErrorState.iconResId)
        buttonAction.setText(sellErrorState.buttonTextResId)

        textViewSubtitle.text = sellErrorState.getSubtitle(resources)

        buttonAction.setOnClickListener {
            when (sellErrorState) {
                is SellScreenError.ServerError -> {
                    popBackStackTo(MainContainerFragment::class)
                }
            }
        }
    }

    private fun logScreenOpened() {
        when (sellErrorState) {
            is SellScreenError.ServerError -> sellAnalytics.logSellServerErrorOpened()
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
    }
}
