package com.p2p.wowlet.dialog.sendcoins.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.utils.getCurrentFragment
import com.p2p.wowlet.appbase.utils.getNavHostFragment
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.databinding.FragmentSendCoinsBinding
import com.p2p.wowlet.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.fragment.dashboard.dialog.SendCoinDoneDialog
import com.p2p.wowlet.fragment.dashboard.dialog.yourwallets.YourWalletsBottomSheet
import com.p2p.wowlet.fragment.dashboard.view.DashboardFragment
import com.p2p.wowlet.fragment.detailwallet.view.DetailWalletFragment
import com.p2p.wowlet.fragment.qrscanner.view.QrScannerFragment
import com.p2p.wowlet.fragment.swap.dialog.SwapCoinProcessingDialog
import com.wowlet.entities.local.WalletItem
import kotlinx.android.synthetic.main.fragment_send_coins.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.pow

class SendCoinsBottomSheet(
    private var walletItem: WalletItem?,
    private var walletAddress: String,
    private val navigateTo: (destinationId: Int, bundle: Bundle?) -> Unit
) : BottomSheetDialogFragment() {

    private val viewModel: SendCoinsViewModel by viewModel()
    private lateinit var binding: FragmentSendCoinsBinding

    private var bottomSheet: View? = null
    private var feeValue = 0.0
    private var balance = 0.0

    companion object {
        const val WALLET_ADDRESS = "walletAddress"
        const val WALLET_ITEM = "walletItem"
        const val TAG_SEND_COIN = "SendCoinsDialogFragment"
        fun newInstance(
            walletItem: WalletItem? = null,
            walletAddress: String,
            navigateTo: (destinationId: Int, bundle: Bundle?) -> Unit
        ): SendCoinsBottomSheet {
            return SendCoinsBottomSheet(walletItem, walletAddress, navigateTo)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_send_coins, container, false
        )
        binding.run {
            viewModel = this@SendCoinsBottomSheet.viewModel
            this.lifecycleOwner = viewLifecycleOwner
            etWalletAddress.doOnTextChanged { text, start, before, count ->
                walletAddress = text.toString()
            }
            etWalletAddress.setText(walletAddress)
            etCount.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                }
            }
            etCount.requestFocus()
            etCount.doOnTextChanged { text, start, before, count ->
                if (text?.length == 1 && text.last() ==  '.') {
                    etCount.setText("")
                    return@doOnTextChanged
                }
                if (text?.isEmpty() == true) {
                    binding.txtInputInToken.text = ""
                    return@doOnTextChanged
                }
                etCount.setSelection(text?.length!!)
                this@SendCoinsBottomSheet.viewModel.setInputCountInTokens(requireContext(), text.toString())
            }
            etWalletAddress.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    bottomSheet?.let {
                        BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            }

        }
        walletItem?.let {
            viewModel.selectWalletItem(it)
        }
        initObservers()
        initViewCommand()
        viewModel.getWalletData()
        viewModel.getWalletItems()
        viewModel.getFee()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        bottomSheet = dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet) ?: return
        bottomSheet!!.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
    }


    private var processingDialog: SwapCoinProcessingDialog? = null


    private fun initObservers() {
        viewModel.successTransaction.observe(viewLifecycleOwner, {
            processingDialog?.run {
                if (isVisible) {
                    dismiss()
                }
                viewModel.openDoneDialog(it)
            }
        })
        viewModel.errorTransaction.observe(viewLifecycleOwner, { message ->
            processingDialog?.run {
                if (isVisible) {
                    dismiss()
                }
            }
            context?.let {
                Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.feeResponseLiveData.observe(viewLifecycleOwner, {
            feeValue = it.toDouble()
            val feeCount = "$it ${walletItem?.tokenName}"
            binding.feeCount.text = feeCount
        })
        viewModel.feeErrorLiveData.observe(viewLifecycleOwner, { message ->
            context?.run { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }

        })
        viewModel.getWalletData.observe(viewLifecycleOwner, { walletItemList ->
            if (walletItemList.isNotEmpty()) {
                walletItem?.let { item ->
                    val find = walletItemList.find { item1 ->
                        item1.depositAddress == item.depositAddress
                    }
                    find?.let {
                        viewModel.selectWalletItem(it)
                    } ?: viewModel.selectWalletItem(
                        walletItemList[0]
                    )
                }
            }

        })
        viewModel.walletItemData.observe(viewLifecycleOwner) {
            walletItem = it
            viewModel.getFee()
            viewModel.setInputCountInTokens(requireContext(), binding.etCount.text.toString())
            val balanceText = getString(R.string.available, (walletItem?.walletBinds?.let { it1 ->
                walletItem?.amount?.times(
                    it1
                )
            }), viewModel.selectedCurrency.value)
            binding.txtAvailableBalance.text = balanceText
        }
        viewModel.savedWalletItemData.observe(viewLifecycleOwner, {
            if (it.depositAddress.isNotEmpty()) {
                walletItem = it
                viewModel.getWalletItems()
            }
        })
        viewModel.selectedCurrency.observe(viewLifecycleOwner) {
            var amount = walletItem?.amount
            if (it == "USD") {
                amount = walletItem?.walletBinds?.let { it1 -> amount?.times(it1) }
            }
            val balanceText = getString(R.string.available, amount, it)
            binding.txtAvailableBalance.text = balanceText
            viewModel.setInputCountInTokens(requireContext(), binding.etCount.text.toString())
        }
        viewModel.yourBalance.observe(viewLifecycleOwner, {
            balance = it
        })
    }

    private fun initViewCommand() {
        viewModel.command.observe(viewLifecycleOwner, { viewCommand->
            when (viewCommand) {
                is NavigateUpBackStackCommand -> {
                    dismiss()
                    viewModel.setWalletData(null)
                }
                is NavigateScannerFromSendCoinViewCommand -> {
                    walletItem?.let { viewModel.setWalletData(it) }
                    val actionId = when(getNavHostFragment()?.let { getCurrentFragment(it) }) {
                        is DashboardFragment -> R.id.action_navigation_dashboard_to_navigation_scanner
                        is DetailWalletFragment -> R.id.action_navigation_detail_wallet_to_navigation_scanner
                        is QrScannerFragment -> throw IllegalStateException("Must have a destination to ${QrScannerFragment::class}")
                        else -> null
                    }
                    actionId?.let {
                        navigateTo.invoke(it, null)
                    }

                }
                is OpenMyWalletDialogViewCommand -> {
                    val yourWalletsBottomSheet: YourWalletsBottomSheet =
                        YourWalletsBottomSheet.newInstance() {
                            viewModel.selectWalletItem(it)
                        }
                    yourWalletsBottomSheet.show(
                        childFragmentManager,
                        YourWalletsBottomSheet.YOUR_WALLET
                    )
                }
                is SendCoinDoneViewCommand -> {
                    val sendCoinDoneDialog: SendCoinDoneDialog =
                        SendCoinDoneDialog.newInstance(
                            viewCommand.transactionInfo,
                            goToWallet = {
                                dismiss()
                            },
                            navigateBlockChain = { destinationId, bundle ->
                                navigateTo.invoke(destinationId, bundle)
                            })
                    sendCoinDoneDialog.show(childFragmentManager, SendCoinDoneDialog.SEND_COIN_DONE)
                }
                is SendCoinViewCommand -> {
                    with(binding) {

                        val amount = this@SendCoinsBottomSheet.viewModel.walletItemData.value?.amount
                        val decimals =
                            this@SendCoinsBottomSheet.viewModel.walletItemData.value?.decimals
                        amount?.run {
                            decimals?.run {
                                if (walletAddress.isNotEmpty() && etCount.text.toString()
                                        .isNotEmpty()
                                ) {
                                    val amountInTokens: Double = if (viewModel?.selectedCurrency?.value == "USD") {
                                        etCount.text.toString().toDouble().div(walletItem?.walletBinds!!)
                                    }else {
                                        etCount.text.toString().toDouble()
                                    }
                                    if (amountInTokens < walletItem?.amount!! - feeValue) {
                                        processingDialog = SwapCoinProcessingDialog.newInstance {}
                                        processingDialog?.show(
                                            childFragmentManager,
                                            SwapCoinProcessingDialog.SWAP_COIN_PROGRESS
                                        )
                                        val lamprots =
                                            etCount.text.toString().toDouble() * (10.0.pow(
                                                decimals.toDouble()
                                            ))

                                        this@SendCoinsBottomSheet.viewModel.sendCoin(
                                            walletAddress,
                                            lamprots.toLong(),
                                            this@SendCoinsBottomSheet.viewModel.walletItemData.value?.tokenSymbol!!
                                        )
                                    } else {
                                        context?.let {
                                            Toast.makeText(
                                                it,
                                                "There is not enough money", Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                } else {
                                    processingDialog?.dismiss()
                                    context?.let {
                                        Toast.makeText(
                                            it,
                                            "Invalidate input data", Toast.LENGTH_SHORT
                                        ).show()
                                    }
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
        })
    }



}