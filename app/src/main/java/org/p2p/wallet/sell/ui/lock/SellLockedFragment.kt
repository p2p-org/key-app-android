package org.p2p.wallet.sell.ui.lock

import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.databinding.FragmentSellLockBinding
import org.p2p.wallet.newsend.ui.NewSendFragment
import org.p2p.wallet.send.model.AddressState
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.copyToClipBoard
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val ARG_SELL_LOCKED = "ARG_SELL_LOCKED"

class SellLockedFragment :
    BaseMvpFragment<SellLockedContract.View, SellLockedContract.Presenter>(R.layout.fragment_sell_lock),
    SellLockedContract.View {

    companion object {
        fun create(arguments: SellLockedArguments): SellLockedFragment =
            SellLockedFragment()
                .withArgs(ARG_SELL_LOCKED to arguments)
    }

    override val presenter: SellLockedContract.Presenter by inject()
    private val binding: FragmentSellLockBinding by viewBinding()

    private val arguments: SellLockedArguments by args(ARG_SELL_LOCKED)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            textViewAmount.text = "${arguments.solAmount} $SOL_SYMBOL"
            textViewUsdValue.text = getString(R.string.sell_lock_usd_amount, arguments.amountInUsd)
            textViewRecipient.text = arguments.moonpayAddress.cutMiddle()

            imageViewCopy.setOnClickListener {
                requireContext().copyToClipBoard(arguments.moonpayAddress)
                showUiKitSnackBar(messageResId = R.string.common_copied)
            }

            toolbar.setNavigationOnClickListener {
                popBackStack()
            }

            buttonSend.setOnClickListener {
                // add amount and initial token
                // Send team promised they will add new create method for such purposes
                replaceFragment(
                    NewSendFragment.create(
                        SearchResult.AddressOnly(AddressState(arguments.moonpayAddress, NetworkType.SOLANA))
                    )
                )
            }
            buttonRemove.setOnClickListener {
                presenter.removeFromHistory()
            }
        }
    }
}
