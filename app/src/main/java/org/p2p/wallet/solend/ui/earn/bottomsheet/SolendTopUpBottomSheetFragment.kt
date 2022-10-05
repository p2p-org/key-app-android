package org.p2p.wallet.solend.ui.earn.bottomsheet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.uikit.glide.GlideManager
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpBottomSheet
import org.p2p.wallet.databinding.DialogSolendTopUpBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.receive.token.ReceiveTokenFragment
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

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
            val item = deposit
            imageViewClose.setOnClickListener {
                dismissAllowingStateLoss()
            }
            glideManager.load(tokenImageView, item.iconUrl.orEmpty())

            if (item is SolendDepositToken.Active) {
                startAmountView.title = "${item.depositAmount} ${item.tokenSymbol}"
            } else {
                startAmountView.title = item.tokenSymbol
            }
            startAmountView.subtitle = item.tokenName

            endAmountView.usdAmount = "${item.supplyInterest.scaleShort()}%"

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
    }

    override fun showReceive(token: Token) {
        if (token is Token.Active) {
            replaceFragment(ReceiveTokenFragment.create(token))
        } else {
            replaceFragment(ReceiveSolanaFragment.create(token))
        }
    }
}
