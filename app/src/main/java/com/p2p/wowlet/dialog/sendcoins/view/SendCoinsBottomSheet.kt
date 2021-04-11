package com.p2p.wowlet.dialog.sendcoins.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.dashboard.dialog.SendCoinDoneDialog
import com.p2p.wowlet.dashboard.dialog.yourwallets.YourWalletsBottomSheet
import com.p2p.wowlet.dashboard.view.DashboardFragment
import com.p2p.wowlet.databinding.FragmentSendCoinsBinding
import com.p2p.wowlet.deprecated.viewcommand.Command
import com.p2p.wowlet.dialog.SwapCoinProcessingDialog
import com.p2p.wowlet.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.dialog.sendcoins.viewmodel.WalletAddressViewModel
import com.p2p.wowlet.entities.Constants
import com.p2p.wowlet.entities.local.WalletItem
import com.p2p.wowlet.qrscanner.view.QrScannerFragment
import com.p2p.wowlet.utils.bindadapter.imageSourceRadiusDp
import com.p2p.wowlet.utils.popBackStack
import com.p2p.wowlet.utils.replaceFragment
import com.p2p.wowlet.utils.viewbinding.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.pow

class SendCoinsBottomSheet(
    private var walletItem: WalletItem?,
    private var walletAddress: String?
) : BottomSheetDialogFragment() {

    private val viewModel: SendCoinsViewModel by viewModel()
    private val walletAddressViewModel: WalletAddressViewModel by sharedViewModel()
    private val binding: FragmentSendCoinsBinding by viewBinding()

    private var bottomSheet: View? = null
    private var feeValue = 0.0
    private var walletItems: List<WalletItem>? = null
    private var isUserOwnScannedWallet = false
    private var getWalletItemsJob: Job? = null

    companion object {
        const val TAG_SEND_COIN = "SendCoinsDialogFragment"
        fun newInstance(
            walletItem: WalletItem? = null,
            walletAddress: String?
        ): SendCoinsBottomSheet {
            return SendCoinsBottomSheet(walletItem, walletAddress)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_send_coins, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            txtAvailableBalance.setOnClickListener { viewModel.insertAllBalance() }
            imgWalletData.setOnClickListener { viewModel.openMyWalletsDialog() }
            imgWalletData.imageSourceRadiusDp(walletItem?.icon, 16)
            txtToken.text = walletItem?.tokenSymbol
            sendCoinButton.setOnClickListener { viewModel.sendCoinCommand() }
            etWalletAddress.doAfterTextChanged { text ->
                if (text == null) return@doAfterTextChanged
                if (text.length == Constants.PUBLIC_KEY_LENGTH) {
                    this@SendCoinsBottomSheet.viewModel.setWalletIconVisibility(true)
                } else {
                    this@SendCoinsBottomSheet.viewModel.setWalletIconVisibility(false)
                }
                if (text.isEmpty()) {
                    imgClearWalletAddress.isVisible = false
                    imgScanQrCode.isVisible = true
                    return@doAfterTextChanged
                }
                imgScanQrCode.setOnClickListener {
                    replaceFragment(QrScannerFragment.create(true))
                }
                imgClearWalletAddress.isVisible = true
                imgScanQrCode.isVisible = false
                for (char in text) {
                    val charToInt = char.toInt()
                    if (charToInt in 1040..1103 ||
                        charToInt == 1025 ||
                        charToInt == 1105
                    ) {
                        etWalletAddress.setText(text.toString().replace(char.toString(), ""))
                        etWalletAddress.setSelection(text.length - 1)
                        return@doAfterTextChanged
                    }
                }
                walletAddress = text.toString()
            }
            etWalletAddress.setText(walletAddress)
            etCount.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                }
            }
            etCount.requestFocus()
            etCount.doOnTextChanged { text, _, _, _ ->
                if (text?.length == 1 && text.last() == '.') {
                    etCount.setText("")
                    return@doOnTextChanged
                }
                this@SendCoinsBottomSheet.viewModel.isAmountBiggerThanAvailable(text.toString())
                if (text?.isEmpty() == true) {
                    txtInputInToken.text = ""
                    return@doOnTextChanged
                }
                etCount.setSelection(text?.length!!)
                this@SendCoinsBottomSheet.viewModel.setInputCountInTokens(
                    requireContext(),
                    text.toString()
                )
            }
            etWalletAddress.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    bottomSheet?.let {
                        BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
                    }
                    etWalletAddress.setSelection(etWalletAddress.text.toString().length)
                }
            }
        }
        walletItem?.let {
            viewModel.selectWalletItem(it)
        }
        initObservers()
        initViewCommand()
        viewModel.getWalletData()
        getWalletItemsJob = viewModel.getWalletItems()
        viewModel.getFee()
    }

    override fun onStart() {
        super.onStart()
        bottomSheet = dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet) ?: return
        bottomSheet?.layoutParams?.height = WindowManager.LayoutParams.MATCH_PARENT
        BottomSheetBehavior.from(bottomSheet!!).apply {
            state = BottomSheetBehavior.STATE_HALF_EXPANDED
            halfExpandedRatio = 0.99f
        }
    }

    private var processingDialog: SwapCoinProcessingDialog? = null

    private fun initObservers() {
        viewModel.successTransaction.observe(
            viewLifecycleOwner,
            {
                processingDialog?.run {
                    if (isVisible) {
                        dismiss()
                    }
                    viewModel.openDoneDialog(it)
                }
            }
        )
        viewModel.isInsertedMoreThanAvailable.observe(viewLifecycleOwner) {
            val color = if (it) R.color.red_500 else R.color.blue_400
            binding.txtAvailableBalance.setTextColor(ContextCompat.getColor(requireContext(), color))
        }
        viewModel.walletIconVisibility.observe(viewLifecycleOwner) {
            binding.imgWallet.isVisible = it
        }
        viewModel.errorTransaction.observe(
            viewLifecycleOwner,
            { message ->
                processingDialog?.run {
                    if (isVisible) {
                        dismiss()
                    }
                }
                context?.let {
                    Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
                }
            }
        )
        viewModel.feeResponseLiveData.observe(
            viewLifecycleOwner,
            {
                feeValue = it.toDouble()
                val feeCount = "$it ${walletItem?.tokenName}"
                binding.feeCount.text = feeCount
            }
        )
        viewModel.feeErrorLiveData.observe(
            viewLifecycleOwner,
            { message ->
                context?.run { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
            }
        )
        viewModel.getWalletData.observe(
            viewLifecycleOwner,
            { walletItemList ->
                walletItems = walletItemList
                if (walletItemList.isNotEmpty()) {
                    if (!walletItem?.depositAddress.isNullOrEmpty()) {
                        val find = walletItemList.find { item ->
                            item.depositAddress == walletItem!!.depositAddress
                        }
                        find?.let {
                            viewModel.selectWalletItem(it)
                        }
                    } else {
                        val find = walletItemList.find { item ->
                            item.depositAddress == walletAddress
                        }
                        find?.let {
                            viewModel.selectWalletItem(it)
                        }
                    }
                }
            }
        )
        viewModel.isInsertedMoreThanAvailable.observe(viewLifecycleOwner) {
            binding.sendCoinButton.isEnabled = !it
        }
        viewModel.walletItemData.observe(viewLifecycleOwner) {
            if (it.tokenSymbol == "") return@observe
            binding.imgWalletData.imageSourceRadiusDp(it.icon, 16)
            binding.txtToken.text = it.tokenSymbol

            viewModel.getFee()
            viewModel.setInputCountInTokens(requireContext(), binding.etCount.text.toString())
            val balanceText = getString(R.string.available, it.amount, it.tokenSymbol)
            binding.txtAvailableBalance.text = balanceText
            viewModel.setSelectedCurrency(it.tokenSymbol)
            if (it.walletBinds != 0.0) {
                isUserOwnScannedWallet = true
            }
        }
//        viewModel.savedWalletItemData.observe(viewLifecycleOwner, {
//            if (it.depositAddress.isNotEmpty()) {
//                walletItem = it
//                viewModel.getWalletItems()
//            }
//        })
        viewModel.selectedCurrency.observe(viewLifecycleOwner) {
            var amount = walletItem?.amount
            if (it == "USD") {
                amount = walletItem?.walletBinds?.let { it1 -> amount?.times(it1) }
            }
            amount?.let { availableAmount -> viewModel.setAvailableAmountInSelectedCurrency(availableAmount) }
            val balanceText = getString(R.string.available, amount, it)
            binding.txtAvailableBalance.text = balanceText
            viewModel.setInputCountInTokens(requireContext(), binding.etCount.text.toString())
        }
        walletAddressViewModel.walletData.observe(viewLifecycleOwner) { walletData ->
            if (walletAddressViewModel.disableObserving) return@observe
            binding.apply {
                etWalletAddress.setText(walletData.walletAddress)
                etWalletAddress.clearFocus()
                etCount.clearFocus()
                etCount.setText(walletAddressViewModel.enteredAmount)
                imgClearWalletAddress.isVisible = true
                imgScanQrCode.isVisible = false
            }
            getWalletItemsJob?.invokeOnCompletion {
                runBlocking {
                    withContext(Dispatchers.Main) {
                        walletItems?.let { myWallets ->
                            val find = myWallets.find {
                                it.mintAddress == walletData.walletItem.mintAddress
                            }
                            find?.let {
                                isUserOwnScannedWallet = true
                                viewModel.selectWalletItem(it)
                            } ?: viewModel.selectFromConstWalletItems(walletData)
                        }
                        viewModel.setSelectedCurrency(walletData.walletItem.tokenSymbol)
                        walletAddressViewModel.disableObserving = true
                    }
                }
            }
        }
        viewModel.clearWalletAddress.observe(viewLifecycleOwner) {
            if (!it) return@observe
            binding.apply {
                etWalletAddress.setText("")
                etWalletAddress.requestFocus()
                imgClearWalletAddress.isVisible = false
                imgScanQrCode.isVisible = true
            }
            viewModel.disableClearWalletAddress()
        }
        viewModel.saveEnteredAmount.observe(viewLifecycleOwner) {
            walletAddressViewModel.enteredAmount = binding.etCount.text.toString()
        }
        walletAddressViewModel.enteredAmountLV.observe(viewLifecycleOwner) {
            binding.etCount.setText(it)
        }
    }

    private fun initViewCommand() {
        viewModel.command.observe(
            viewLifecycleOwner,
            { viewCommand ->
                when (viewCommand) {
                    is Command.NavigateScannerViewCommand -> {
                        when (requireActivity().supportFragmentManager.findFragmentById(R.id.container)) {
                            is DashboardFragment -> {
                                replaceFragment(QrScannerFragment.create(viewCommand.goBack))
                            }
                            else -> {
                                popBackStack()
                                this.dismiss()
                            }
                        }
                    }
                    is Command.OpenMyWalletDialogViewCommand -> {
                        val yourWalletsBottomSheet: YourWalletsBottomSheet =
                            YourWalletsBottomSheet.newInstance(
                                getString(R.string.select_wallet)
                            ) {
                                viewModel.selectWalletItem(it)
                            }
                        yourWalletsBottomSheet.show(
                            childFragmentManager,
                            YourWalletsBottomSheet.YOUR_WALLET
                        )
                    }
                    is Command.SendCoinDoneViewCommand -> {
                        val sendCoinDoneDialog: SendCoinDoneDialog =
                            SendCoinDoneDialog.newInstance(
                                viewCommand.transactionInfo,
                                navigateBack = {
                                    dismiss()
                                }
                            )
                        sendCoinDoneDialog.show(childFragmentManager, SendCoinDoneDialog.SEND_COIN_DONE)
                    }
                    is Command.SendCoinViewCommand -> {
                        with(binding) {

                            val amount =
                                this@SendCoinsBottomSheet.viewModel.walletItemData.value?.amount
                            val decimals =
                                this@SendCoinsBottomSheet.viewModel.walletItemData.value?.decimals
                            amount?.run {
                                decimals?.run {
                                    if (walletAddress?.isNotEmpty() == true && etCount.text.toString()
                                        .isNotEmpty()
                                    ) {
                                        val amountInTokens: Double = if (viewModel?.selectedCurrency?.value == "USD") {
                                            etCount.text.toString().toDouble().div(walletItem?.walletBinds!!)
                                        } else {
                                            etCount.text.toString().toDouble()
                                        }
                                        if (amountInTokens < walletItem?.amount!! - feeValue) {
                                            processingDialog = SwapCoinProcessingDialog.newInstance {}
                                            processingDialog?.show(
                                                childFragmentManager,
                                                SwapCoinProcessingDialog.SWAP_COIN_PROGRESS
                                            )
                                            val lamprots =
                                                etCount.text.toString().toDouble() * (
                                                    10.0.pow(
                                                        decimals.toDouble()
                                                    )
                                                    )

                                            this@SendCoinsBottomSheet.viewModel.sendCoin(
                                                walletAddress!!,
                                                lamprots.toLong(),
                                                this@SendCoinsBottomSheet.viewModel.walletItemData.value?.tokenSymbol!!
                                            )
                                        } else {
                                            if (isUserOwnScannedWallet) {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "There is not enough money",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "You don't have corresponding wallet, please add one",
                                                    Toast.LENGTH_LONG
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
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        walletAddressViewModel.enteredAmount = ""
    }
}