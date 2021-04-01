package com.p2p.wowlet.fragment.dashboard.dialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogSendCoinDoneBinding
import com.p2p.wowlet.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.fragment.blockchainexplorer.view.BlockChainExplorerFragment
import com.p2p.wowlet.utils.copyClipboard
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.replace
import com.wowlet.entities.Constants.Companion.EXPLORER_SOLANA
import com.wowlet.entities.local.ActivityItem
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal

class SendCoinDoneDialog(
    val transactionInfo: ActivityItem,
    private val navigateBack: () -> Unit
) : DialogFragment() {

    private val sendCoinsViewModel: SendCoinsViewModel by viewModel()
    lateinit var binding: DialogSendCoinDoneBinding

    companion object {
        const val SEND_COIN_DONE = "SEND_COIN_DONE"
        fun newInstance(
            transactionInfo: ActivityItem,
            navigateBack: () -> Unit
        ): SendCoinDoneDialog =
            SendCoinDoneDialog(transactionInfo, navigateBack)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_send_coin_done, container, false
        )
        binding.viewModel = sendCoinsViewModel
        binding.model = transactionInfo
        binding.backButton.setOnClickListener {
            dismissAllowingStateLoss()
            popBackStack()
        }
        binding.blockChainExplorer.setOnClickListener {
            replace(BlockChainExplorerFragment.createScreen(EXPLORER_SOLANA + transactionInfo.signature))
            dismissAllowingStateLoss()
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.copyTransaction.setOnClickListener {
            context?.copyClipboard(transactionInfo.signature)
        }

        binding.price.text =
            transactionInfo.symbolsPrice + BigDecimal.valueOf(transactionInfo.price) + " " + transactionInfo.tokenSymbol
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