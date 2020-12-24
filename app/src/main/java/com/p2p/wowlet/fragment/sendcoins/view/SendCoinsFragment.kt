package com.p2p.wowlet.fragment.sendcoins.view

import android.annotation.SuppressLint
import android.widget.Toast
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentSendCoinsBinding
import com.p2p.wowlet.fragment.dashboard.dialog.yourwallets.YourWalletsBottomSheet
import com.p2p.wowlet.fragment.sendcoins.dialog.SendCoinDoneDialog
import com.p2p.wowlet.fragment.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.fragment.swap.dialog.SwapCoinProcessingDialog
import com.wowlet.entities.local.UserWalletType
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.pow

class SendCoinsFragment : FragmentBaseMVVM<SendCoinsViewModel, FragmentSendCoinsBinding>() {

    override val viewModel: SendCoinsViewModel by viewModel()
    override val binding: FragmentSendCoinsBinding by dataBinding(R.layout.fragment_send_coins)

    private var feeValue = 0.0
    private var balance = 0.0

    companion object {
        const val WALLET_ADDRESS = "walletAddress"
    }

    private var walletAddress: String = ""
    private var processingDialog: SwapCoinProcessingDialog? = null
    override fun initView() {
        binding.run {
            viewModel = this@SendCoinsFragment.viewModel
        }
        viewModel.initData(
            mutableListOf(
                UserWalletType(
                    "Wallet address",
                    walletAddress,
                    false,
                    R.drawable.ic_qr_scaner
                ),
                UserWalletType("Wallet user", "@username", true, R.drawable.ic_account)
            )
        )
        viewModel.getWalletItems()
        viewModel.getFee()
    }

    override fun initData() {
        arguments?.let {
            walletAddress = it.getString(WALLET_ADDRESS, "")
        }
    }

    @SuppressLint("SetTextI18n")
    override fun observes() {
        observe(viewModel.successTransaction) {
            processingDialog?.run {
                if (isVisible) {
                    dismiss()
                }
                viewModel.openDoneDialog(it)
            }
        }
        observe(viewModel.errorTransaction) { message ->
            processingDialog?.run {
                if (isVisible) {
                    dismiss()
                }
            }
            context?.let {
                Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
            }
        }

        observe(viewModel.feeResponseLiveData) {
            feeValue = it.toDouble()
            binding.feeCount.text = "$it SOL"
        }

        observe(viewModel.feeErrorLiveData) { message ->
            context?.run { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
        }
        observe(viewModel.yourBalance) { balance = it }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is NavigateUpViewCommand -> {
                navigateBackStack()
            }
            is NavigateScannerViewCommand -> {
                navigateFragment(command.destinationId)
                (activity as MainActivity).showHideNav(false)
            }
            is OpenMyWalletDialogViewCommand -> {
                val yourWalletsBottomSheet: YourWalletsBottomSheet =
                    YourWalletsBottomSheet.newInstance() {
                        walletAddress = it.depositAddress
                        viewModel.selectWalletItem(it)
                    }
                yourWalletsBottomSheet.show(
                    childFragmentManager,
                    YourWalletsBottomSheet.YOUR_WALLET
                )
            }
            is SendCoinDoneViewCommand -> {
                val sendCoinDoneDialog: SendCoinDoneDialog =
                    SendCoinDoneDialog.newInstance(command.transactionInfo) {
                        navigateUp()
                    }
                sendCoinDoneDialog.show(childFragmentManager, SendCoinDoneDialog.SEND_COIN_DONE)
            }
            is SendCoinViewCommand -> {
                with(binding) {
                    if ((feeValue + etCount.text.toString().toDouble()) < balance) {
                        val amount = this@SendCoinsFragment.viewModel.walletItemData.value?.amount
                        val decimals =
                            this@SendCoinsFragment.viewModel.walletItemData.value?.decimals
                        amount?.run {
                            decimals?.run {
                                if (walletAddress.isNotEmpty() && etCount.text.toString()
                                        .isNotEmpty()
                                ) {
                                    processingDialog = SwapCoinProcessingDialog.newInstance {}
                                    processingDialog?.show(
                                        childFragmentManager,
                                        SwapCoinProcessingDialog.SWAP_COIN_PROGRESS
                                    )
                                    val lamprots = etCount.text.toString().toDouble() * (10.0.pow(
                                        decimals.toDouble()
                                    ))
                                    this@SendCoinsFragment.viewModel.sendCoin(
                                        walletAddress,
                                        lamprots.toLong()
                                    )
                                } else
                                    processingDialog?.dismiss()
                                context?.let {
                                    Toast.makeText(
                                        it,
                                        "Invalidate input data", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } ?: context?.let {
                                processingDialog?.dismiss()
                                Toast.makeText(
                                    it,
                                    "Not Selected wallet", Toast.LENGTH_SHORT
                                ).show()
                            }
                        } ?: context?.let {
                            processingDialog?.dismiss()
                            Toast.makeText(
                                it,
                                "Not Selected wallet", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun navigateUp() {
        navigateBackStack()
        //viewModel.navigateUp()
    }

}