package com.p2p.wowlet.fragment.sendcoins.dialog

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
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.databinding.DialogSendCoinDoneBinding
import com.p2p.wowlet.fragment.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.utils.copyClipboard
import com.wowlet.entities.Constants
import com.wowlet.entities.local.ActivityItem
import kotlinx.android.synthetic.main.dialog_send_coin_done.*
import kotlinx.android.synthetic.main.dialog_send_coin_done.copyTransaction
import kotlinx.android.synthetic.main.dialog_tansaction.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal

class SendCoinDoneDialog(
    val transactionInfo: ActivityItem,
    private val goToWallet: () -> Unit,
    private val navigateBlockChain: (destinationId: Int, bundle: Bundle?) -> Unit
) : DialogFragment() {

    private val sendCoinsViewModel: SendCoinsViewModel by viewModel()
    lateinit var binding: DialogSendCoinDoneBinding

    companion object {
        const val SEND_COIN_DONE = "SEND_COIN_DONE"
        fun newInstance(
            transactionInfo: ActivityItem,
            goToWallet: () -> Unit,
            navigateBlockChain: (destinationId: Int, bundle: Bundle?) -> Unit
        ): SendCoinDoneDialog =
            SendCoinDoneDialog(transactionInfo, goToWallet, navigateBlockChain)
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
        binding.blockChainExplorer.setOnClickListener {
            sendCoinsViewModel.goToBlockChainExplorer(transactionInfo.signature)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendCoinsViewModel.command.observe(viewLifecycleOwner, {
            when (it) {
                is Command.NavigateBlockChainViewCommand -> {
                    navigateBlockChain.invoke(it.destinationId, it.bundle)
                }
                is Command.NavigateUpBackStackCommand -> {
                    goToWallet.invoke()
                }
            }

        })
        binding.copyTransaction.setOnClickListener {
            context?.copyClipboard(transactionInfo.signature)
        }

        binding.price.text =
            transactionInfo.symbolsPrice + BigDecimal.valueOf(transactionInfo.price) + transactionInfo.tokenSymbol
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