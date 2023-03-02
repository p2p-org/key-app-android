package org.p2p.wallet.claim.ui

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import java.math.BigDecimal
import org.p2p.core.glide.GlideManager
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentClaimBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_TOKEN_SYMBOL = "ARG_TOKEN_SYMBOL"
private const val ARG_TOKEN_AMOUNT = "ARG_TOKEN_AMOUNT"
private const val ARG_FIAT_AMOUNT = "ARG_FIAT_AMOUNT"

private const val IMAGE_SIZE_DP = 64

class ClaimFragment :
    BaseMvpFragment<ClaimContract.View, ClaimContract.Presenter>(R.layout.fragment_claim),
    ClaimContract.View {

    companion object {
        fun create(
            tokenSymbol: String,
            tokenAmount: BigDecimal,
            fiatAmount: BigDecimal
        ) = ClaimFragment().withArgs(
            ARG_TOKEN_SYMBOL to tokenSymbol,
            ARG_TOKEN_AMOUNT to tokenAmount,
            ARG_FIAT_AMOUNT to fiatAmount,
        )
    }

    override val presenter: ClaimContract.Presenter by inject()

    private val binding: FragmentClaimBinding by viewBinding()

    private val glideManager: GlideManager by inject()

    private val tokenSymbol: String by args(ARG_TOKEN_SYMBOL)
    private val tokenAmount: BigDecimal by args(ARG_TOKEN_AMOUNT)
    private val fiatAmount: BigDecimal by args(ARG_FIAT_AMOUNT)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            buttonBottom.setOnClickListener {
                presenter.onSendButtonClicked()
            }
            presenter.loadData(
                tokenSymbol = tokenSymbol,
                tokenAmount = tokenAmount,
                fiatAmount = fiatAmount
            )
            // TODO PWN-7377 add icon of token setting to view and use real data
        }
    }

    override fun setTitle(title: String) {
        binding.toolbar.title = title
    }

    override fun setTokenIconUrl(tokenIconUrl: String) {
        glideManager.load(
            imageView = binding.imageViewToken,
            url = tokenIconUrl,
            size = IMAGE_SIZE_DP,
            circleCrop = true
        )
    }

    override fun setTokenAmount(tokenAmount: String) {
        binding.textViewTokenAmount.text = tokenAmount
    }

    override fun setFiatAmount(fiatAmount: String) {
        binding.textViewFiatAmount.text = fiatAmount
    }

    override fun showFee(fee: String) {
        binding.textViewFeeValue.text = fee
    }
}
