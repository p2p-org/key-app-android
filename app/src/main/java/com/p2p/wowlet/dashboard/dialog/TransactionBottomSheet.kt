package com.p2p.wowlet.dashboard.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogTansactionBinding
import com.p2p.wowlet.deprecated.viewcommand.Command
import com.p2p.wowlet.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wowlet.domain.utils.getTransactionDate
import com.p2p.wowlet.entities.Constants.Companion.EXPLORER_SOLANA
import com.p2p.wowlet.entities.local.ActivityItem
import com.p2p.wowlet.utils.bindadapter.walletFormat
import com.p2p.wowlet.utils.copyClipboard
import com.p2p.wowlet.utils.replaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding
import com.p2p.wowlet.view.BlockChainExplorerFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransactionBottomSheet(private val dataInfo: ActivityItem, val navigate: (url: String) -> Unit) :
    BottomSheetDialogFragment() {

    companion object {
        const val TRANSACTION_DIALOG = "transactionDialog"
        fun newInstance(dataInfo: ActivityItem, navigate: (url: String) -> Unit): TransactionBottomSheet =
            TransactionBottomSheet(dataInfo, navigate)
    }

    private val viewModel: DetailWalletViewModel by viewModel()

    private val binding: DialogTansactionBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.dialog_tansaction, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getBlockTime(dataInfo.slot)
        binding.apply {
            setTransactionImage()

            fromTextView.walletFormat(dataInfo.from, 4)
            toTextView.walletFormat(dataInfo.to, 4)

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
                replaceFragment(BlockChainExplorerFragment.createScreen(EXPLORER_SOLANA + dataInfo.signature))
            }

            txtType.setText(if (dataInfo.isReceive) R.string.receive else R.string.send)
        }
        observes()
    }

    private fun setTransactionImage() {
        val imageTransaction = if (dataInfo.isReceive) R.drawable.ic_receive else R.drawable.ic_send
        binding.imgTransactionType.setImageResource(imageTransaction)
    }

    private fun observes() {
        viewModel.blockTime.observe(
            viewLifecycleOwner,
            {
                binding.yourTransactionDate.text = it.getTransactionDate()
            }
        )
        viewModel.blockTimeError.observe(
            viewLifecycleOwner,
            {
                binding.yourTransactionDate.text = it.getTransactionDate()
            }
        )

        viewModel.command.observe(
            viewLifecycleOwner,
            {
                when (it) {
                    is Command.NavigateBlockChainViewCommand -> {
                        navigate.invoke(it.url)
                    }
                }
            }
        )
    }
}