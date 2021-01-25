package com.p2p.wowlet.fragment.dashboard.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.databinding.DialogTansactionBinding
import com.p2p.wowlet.fragment.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wowlet.utils.copyClipboard
import com.wowlet.domain.utils.getTransactionDate
import com.wowlet.entities.Constants.Companion.EXPLORER_SOLANA
import com.wowlet.entities.local.ActivityItem
import kotlinx.android.synthetic.main.dialog_tansaction.*
import org.koin.androidx.viewmodel.ext.android.viewModel


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
        viewModel.getBlockTime(dataInfo.slot)
        binding.apply {
            model = dataInfo
            setTransactionImage(this)
        }
        return binding.root
    }

    private fun setTransactionImage(binding: DialogTansactionBinding) {
        val imageTransaction = if (dataInfo.isReceive) R.drawable.ic_receive else R.drawable.ic_send
        binding.imgTransactionType.setImageResource(imageTransaction)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            binding.yourTransactionDate.text=it.getTransactionDate()
        })
        viewModel.blockTimeError.observe(viewLifecycleOwner,{
            binding.yourTransactionDate.text=it.getTransactionDate()
        })

        viewModel.command.observe(viewLifecycleOwner,{
           when(it){
               is Command.NavigateBlockChainViewCommand-> {
                   navigate.invoke(it.destinationId,it.bundle)
               }
           }
        })
    }

}