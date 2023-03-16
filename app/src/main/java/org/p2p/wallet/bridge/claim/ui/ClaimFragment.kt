package org.p2p.wallet.bridge.claim.ui

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.glide.GlideManager
import org.p2p.core.token.Token
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.bridge.claim.model.ClaimDetails
import org.p2p.wallet.bridge.claim.ui.dialogs.ClaimInfoBottomSheet
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentClaimBinding
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_TOKEN = "ARG_TOKEN"

private const val IMAGE_SIZE_DP = 64

class ClaimFragment :
    BaseMvpFragment<ClaimContract.View, ClaimContract.Presenter>(R.layout.fragment_claim),
    ClaimContract.View {

    companion object {
        fun create(ethereumToken: Token.Eth) =
            ClaimFragment()
                .withArgs(ARG_TOKEN to ethereumToken)
    }

    private val token: Token.Eth by args(ARG_TOKEN)

    override val presenter: ClaimContract.Presenter by inject { parametersOf(token) }

    private val binding: FragmentClaimBinding by viewBinding()

    private val glideManager: GlideManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            layoutFeeContainer.setOnClickListener { presenter.onFeeClicked() }
            buttonBottom.setOnClickListener { presenter.onSendButtonClicked() }
        }
    }

    override fun setTitle(title: String) {
        binding.toolbar.title = title
    }

    override fun setTokenIconUrl(tokenIconUrl: String?) {
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

    override fun showClaimFeeInfo(claimDetails: ClaimDetails) {
        ClaimInfoBottomSheet.show(childFragmentManager, claimDetails)
    }

    override fun showClaimButtonValue(tokenAmountToClaim: String) {
        binding.buttonBottom.text = getString(R.string.bridge_claim_bottom_button_format, tokenAmountToClaim)
    }

    override fun setClaimButtonState(isButtonEnabled: Boolean) {
        with(binding.buttonBottom) {
            isEnabled = isButtonEnabled
            if (isButtonEnabled) {
                setTextColorRes(R.color.text_snow)
                setBackgroundColor(getColor(R.color.bg_night))
            } else {
                setTextColorRes(R.color.text_mountain)
                setBackgroundColor(getColor(R.color.bg_rain))
            }
        }
    }
}
