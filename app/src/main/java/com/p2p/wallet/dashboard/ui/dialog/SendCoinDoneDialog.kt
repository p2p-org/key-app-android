package com.p2p.wallet.dashboard.ui.dialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.p2p.wallet.R
import com.p2p.wallet.databinding.DialogSendCoinDoneBinding
import com.p2p.wallet.dashboard.ui.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wallet.common.network.Constants.Companion.EXPLORER_SOLANA
import com.p2p.wallet.dashboard.model.local.ActivityItem
import com.p2p.wallet.utils.copyClipboard
import com.p2p.wallet.utils.popBackStack
import com.p2p.wallet.utils.replaceFragment
import com.p2p.wallet.utils.viewbinding.viewBinding
import com.p2p.wallet.blockchain.BlockChainExplorerFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal

class SendCoinDoneDialog(
    private val transactionInfo: ActivityItem,
    private val navigateBack: () -> Unit
) : DialogFragment() {

    private val viewModel: SendCoinsViewModel by viewModel()

    companion object {
        const val SEND_COIN_DONE = "SEND_COIN_DONE"
        fun newInstance(
            transactionInfo: ActivityItem,
            navigateBack: () -> Unit
        ): SendCoinDoneDialog =
            SendCoinDoneDialog(transactionInfo, navigateBack)
    }

    private val binding: DialogSendCoinDoneBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_send_coin_done, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            signatureTextView.text = transactionInfo.signature

            binding.backButton.setOnClickListener {
                dismissAllowingStateLoss()
                popBackStack()
            }
            binding.blockChainExplorer.setOnClickListener {
                replaceFragment(BlockChainExplorerFragment.createScreen(EXPLORER_SOLANA + transactionInfo.signature))
                dismissAllowingStateLoss()
            }
            binding.copyTransaction.setOnClickListener {
                context?.copyClipboard(transactionInfo.signature)
            }

            binding.price.text =
                transactionInfo.symbolsPrice + BigDecimal.valueOf(
                transactionInfo.price
            ) + " " + transactionInfo.tokenSymbol
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            window?.setLayout(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable = false
        }
    }
}