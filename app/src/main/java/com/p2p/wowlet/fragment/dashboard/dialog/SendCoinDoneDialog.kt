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
import com.p2p.wowlet.appbase.utils.getCurrentFragment
import com.p2p.wowlet.appbase.utils.getNavHostFragment
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.databinding.DialogSendCoinDoneBinding
import com.p2p.wowlet.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.fragment.dashboard.view.DashboardFragment
import com.p2p.wowlet.fragment.detailwallet.view.DetailWalletFragment
import com.p2p.wowlet.fragment.qrscanner.view.QrScannerFragment
import com.p2p.wowlet.utils.copyClipboard
import com.wowlet.entities.Constants.Companion.EXPLORER_SOLANA
import com.wowlet.entities.local.ActivityItem
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.IllegalStateException
import java.math.BigDecimal

class SendCoinDoneDialog(
    val transactionInfo: ActivityItem,
    private val navigateBack: () -> Unit,
    private val navigateBlockChain: (destinationId: Int, bundle: Bundle?) -> Unit
) : DialogFragment() {

    private val sendCoinsViewModel: SendCoinsViewModel by viewModel()
    lateinit var binding: DialogSendCoinDoneBinding

    companion object {
        const val SEND_COIN_DONE = "SEND_COIN_DONE"
        fun newInstance(
            transactionInfo: ActivityItem,
            navigateBack: () -> Unit,
            navigateBlockChain: (destinationId: Int, bundle: Bundle?) -> Unit
        ): SendCoinDoneDialog =
            SendCoinDoneDialog(transactionInfo, navigateBack, navigateBlockChain)
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
            val actionId = when(getNavHostFragment()?.let { navHostFragment -> getCurrentFragment(navHostFragment) }) {
                is DashboardFragment -> R.id.action_navigation_dashboard_to_navigation_block_chain_explorer
                is QrScannerFragment -> throw IllegalStateException("${QrScannerFragment::class} must have a destination id to blockchain explorer")
                else -> null
            }
            actionId?.let {
                sendCoinsViewModel.goToBlockChainExplorer(it, EXPLORER_SOLANA + transactionInfo.signature)
            }
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendCoinsViewModel.command.observe(viewLifecycleOwner, {
            when (it) {
                is Command.NavigateBlockChainViewCommand -> {
                    navigateBlockChain.invoke(it.destinationId, it.bundle)
                }
                is Command.NavigateUpBackStackCommand -> {
                    dismiss()
                    navigateBack.invoke()
                }
            }

        })
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