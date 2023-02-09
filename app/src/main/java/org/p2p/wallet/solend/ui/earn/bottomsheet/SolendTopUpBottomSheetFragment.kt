package org.p2p.wallet.solend.ui.earn.bottomsheet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.core.glide.GlideManager
import org.p2p.core.token.Token
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogSolendTopUpBinding
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.receive.token.ReceiveTokenFragment
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import java.math.BigDecimal

private const val EXTRA_DEPOSIT = "EXTRA_STATE"

class SolendTopUpBottomSheetFragment :
    BaseMvpBottomSheet<SolendTopUpBottomSheetContract.View, SolendTopUpBottomSheetContract.Presenter>(
        R.layout.dialog_solend_top_up
    ),
    SolendTopUpBottomSheetContract.View {

    companion object {
        fun show(fragmentManager: FragmentManager, deposit: SolendDepositToken) {
            SolendTopUpBottomSheetFragment()
                .withArgs(EXTRA_DEPOSIT to deposit)
                .show(fragmentManager, SolendTopUpBottomSheetContract::javaClass.name)
        }

        fun hide(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(SolendTopUpBottomSheetContract::javaClass.name)
            (dialog as? SolendTopUpBottomSheetFragment)?.dismissAllowingStateLoss()
        }
    }

    private val deposit: SolendDepositToken by args(EXTRA_DEPOSIT)

    private val binding: DialogSolendTopUpBinding by viewBinding()

    private val glideManager: GlideManager by inject()

    override val presenter: SolendTopUpBottomSheetContract.Presenter by inject {
        parametersOf(deposit)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val depositToTopUp = deposit
            imageViewClose.setOnClickListener {
                dismissAllowingStateLoss()
            }
            glideManager.load(imageViewToken, depositToTopUp.iconUrl.orEmpty())

            if (depositToTopUp is SolendDepositToken.Active) {
                amountViewStart.title = "${depositToTopUp.depositAmount.formatToken()} ${depositToTopUp.tokenSymbol}"
            } else {
                amountViewStart.title = depositToTopUp.tokenSymbol
            }
            amountViewStart.subtitle = depositToTopUp.tokenName

            val supplyInterestToShow = depositToTopUp.supplyInterest ?: BigDecimal.ZERO
            amountViewEnd.topValue = "${supplyInterestToShow.scaleShort()}%"

            buttonBuy.setOnClickListener {
                presenter.onBuyClicked()
            }
            buttonReceive.setOnClickListener {
                presenter.onReceiveClicked()
            }
        }
    }

    override fun getTheme(): Int = R.style.WalletTheme_BottomSheet_Rounded

    override fun onStart() {
        super.onStart()
        BottomSheetBehavior.from(requireView().parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    override fun showBuyScreen(token: Token) {
        replaceFragment(NewBuyFragment.create(token))
        dismissAllowingStateLoss()
    }

    override fun showReceiveScreen(token: Token) {
        if (token is Token.Active) {
            replaceFragment(ReceiveTokenFragment.create(token))
        } else {
            replaceFragment(ReceiveSolanaFragment.create(token))
        }
        dismissAllowingStateLoss()
    }
}
