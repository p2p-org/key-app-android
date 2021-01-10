package com.p2p.wowlet.fragment.dashboard.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.databinding.DialogTansactionBinding
import com.p2p.wowlet.fragment.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wowlet.utils.copyClipboard
import com.p2p.wowlet.utils.roundCurrencyValue
import com.wowlet.entities.Constants.Companion.EXPLORER_SOLANA
import com.wowlet.entities.local.ActivityItem
import kotlinx.android.synthetic.main.dialog_tansaction.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal
import kotlin.math.pow


class TransactionBottomSheet(private val dataInfo: ActivityItem, val navigate:(destinationId:Int,bundle: Bundle?)->Unit) : BottomSheetDialogFragment() {

    lateinit var binding: DialogTansactionBinding
    private val viewModel:DetailWalletViewModel by viewModel()
    companion object {
        const val TRANSACTION_DIALOG="transactionDialog"
        fun newInstance(dataInfo: ActivityItem, navigate:(destinationId:Int,bundle: Bundle?)->Unit): TransactionBottomSheet =
            TransactionBottomSheet(dataInfo,navigate)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_tansaction, container, false
        )
        binding.model = dataInfo
        viewModel.getBlockTime(dataInfo.slot)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vClose.setOnClickListener {
            dismiss()
        }
        copyToUserKey.setOnClickListener {
            context?.copyClipboard(dataInfo.to)
        }
        copyFromUserKey.setOnClickListener {
            context?.copyClipboard(dataInfo.from)
        }
        copyTransaction.setOnClickListener {
            context?.copyClipboard(dataInfo.signature)
        }

        blockChainExplorer.setOnClickListener {
            viewModel.goToBlockChainExplorer(EXPLORER_SOLANA+dataInfo.signature)
        }
        observes()
    }
    private fun observes(){
        viewModel.blockTime.observe(viewLifecycleOwner,{
            binding.yourTransactionDate.text=it
        })
        viewModel.blockTimeError.observe(viewLifecycleOwner,{
            binding.yourTransactionDate.text=it
        })

        viewModel.command.observe(viewLifecycleOwner,{
           when(it){
               is Command.NavigateBlockChainViewCommand-> {
                   navigate.invoke(it.destinationId,it.bundle)
               }
           }
        })
    }
    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isCancelable = false
        }
    }
}