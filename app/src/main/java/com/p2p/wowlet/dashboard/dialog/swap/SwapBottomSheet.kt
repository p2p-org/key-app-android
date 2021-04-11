package com.p2p.wowlet.dashboard.dialog.swap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.dashboard.dialog.SendCoinDoneDialog
import com.p2p.wowlet.dashboard.dialog.swap.dialog.SelectTokenToSwapBottomSheet
import com.p2p.wowlet.dashboard.dialog.swap.dialog.SlippageSettingsBottomSheet
import com.p2p.wowlet.dashboard.dialog.swap.utils.enableDecimalInputTextWatcher
import com.p2p.wowlet.dashboard.dialog.swap.viewmodel.SwapViewModel
import com.p2p.wowlet.dashboard.dialog.yourwallets.YourWalletsBottomSheet
import com.p2p.wowlet.databinding.BottomSheetSwapBinding
import com.p2p.wowlet.deprecated.viewcommand.Command
import com.p2p.wowlet.deprecated.viewcommand.ViewCommand
import com.p2p.wowlet.dialog.SwapCoinProcessingDialog
import com.p2p.wowlet.dialog.utils.makeFullScreen
import com.p2p.wowlet.entities.local.WalletItem
import com.p2p.wowlet.utils.bindadapter.imageSourceRadiusDp
import com.p2p.wowlet.utils.bindadapter.imageSourceRadiusDpWithDefault
import com.p2p.wowlet.utils.toast
import com.p2p.wowlet.utils.viewbinding.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SwapBottomSheet(
    private val allMyWallets: List<WalletItem>,
    private var selectedWalletFrom: WalletItem
) : BottomSheetDialogFragment() {

    companion object {
        const val TAG_SWAP_BOTTOM_SHEET = "swapBottomSheet"
        fun newInstance(
            allMyWallets: List<WalletItem>,
            selectedWalletItems: WalletItem
        ): SwapBottomSheet {
            return SwapBottomSheet(allMyWallets, selectedWalletItems)
        }
    }

    private val viewModel: SwapViewModel by viewModel()

    private val binding: BottomSheetSwapBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.bottom_sheet_swap, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserves()
        viewModel.setSelectedWalletFrom(selectedWalletFrom)
        updateData()
    }

    private fun updateData() {
        binding.run {
            settingsImageView.setOnClickListener { viewModel.openSlippageSettingsBottomSheet() }
            val text = if (viewModel.isInCryptoCurrency.value == true) {
                getString(R.string.available_swap, selectedWalletFrom.amount)
            } else {
                getString(R.string.available_swap, selectedWalletFrom.price)
            }
            availableTextView.text = text

            imgTokenFrom.imageSourceRadiusDp(selectedWalletFrom.icon, 12)
            imgTokenFrom.setOnClickListener { viewModel.openMyWalletsDialog() }
            txtTokenFrom.text = selectedWalletFrom.tokenSymbol
            imgArrowDownFrom.setOnClickListener { viewModel.openMyWalletsDialog() }
            edtAmountFrom.enableDecimalInputTextWatcher(true)
            edtAmountFrom.setText(viewModel.amountBinding.value.toString())

            val aroundToText = if (viewModel.isInCryptoCurrency.value == true) {
                getString(
                    R.string.around_amount_sol_2,
                    viewModel.aroundToCurrency.value?.toDouble(),
                    getString(R.string.usd_symbol)
                )
            } else {
                getString(
                    R.string.around_amount_sol,
                    viewModel.aroundToCurrency.value?.toDouble(),
                    selectedWalletFrom.tokenSymbol
                )
            }
            aroundTo.text = aroundToText

            currencyTypeImageView.setOnClickListener {
                viewModel.swapFromAmountCurrencyTypes()
            }

            balanceTextView.text = getString(R.string.balance_, selectedWalletFrom.amount)

            imgTokenTo.setOnClickListener {
                viewModel.openSelectTokenToSwapBottomSheet()
            }

            imgTokenTo.imageSourceRadiusDpWithDefault(
                selectedWalletFrom.icon, 12, R.drawable.ic_wallet_blue,
                R.drawable.ic_wallet_blue
            )
            txtTokenTo.text = selectedWalletFrom.tokenSymbol
            imgArrowDownTo.setOnClickListener { viewModel.openSelectTokenToSwapBottomSheet() }
            txtAmountTo.text = viewModel.amountInConvertingToken.toString()
            currencyTextView.isVisible = viewModel.selectedWalletTo.value != null
            priceView.isVisible = viewModel.selectedWalletTo.value != null
            lDetails.isVisible = viewModel.selectedWalletTo.value != null

            val sol = if (viewModel.isFromPerTo.value == true) {
                getString(
                    R.string.amount_token_per_token,
                    viewModel.tokenFromPerTokenTo,
                    selectedWalletFrom.tokenSymbol,
                    viewModel.selectedWalletTo.value?.tokenSymbol
                )
            } else {
                getString(
                    R.string.amount_token_per_token,
                    viewModel.tokenToPerTokenFrom,
                    viewModel.selectedWalletTo.value?.tokenSymbol,
                    selectedWalletFrom.tokenSymbol
                )
            }
            solTextView.text = sol

            imgSwapPrice.setOnClickListener {
                viewModel.switchTokenPrices()
            }

            swapButton.setOnClickListener { viewModel.openProcessingDialog() }
        }
    }

    private fun processViewCommand(command: ViewCommand) {
        when (command) {
            is Command.OpenMyWalletDialogViewCommand -> {
                YourWalletsBottomSheet.newInstance(
                    getString(R.string.select_source)
                ) {
                    viewModel.setSelectedWalletFrom(it)
                }.show(
                    childFragmentManager,
                    YourWalletsBottomSheet.YOUR_WALLET
                )
            }
            is Command.OpenSelectTokenToSwapBottomSheet -> {
                SelectTokenToSwapBottomSheet.newInstance(
                    selectedWalletFrom,
                    allMyWallets
                ) {
                    viewModel.setSelectedWalletTo(it)
                }.show(
                    childFragmentManager,
                    SelectTokenToSwapBottomSheet.TAG_SELECT_TOKEN_TO_SWAP
                )
            }
            is Command.OpenSlippageSettingsBottomSheet -> {
                SlippageSettingsBottomSheet.newInstance {
                    toast("Slippage is $it%")
                    dialog?.makeFullScreen(binding.root, this)
                }.show(
                    childFragmentManager,
                    SlippageSettingsBottomSheet.TAG_SLIPPAGE_SETTINGS
                )
            }
            is Command.SwapCoinProcessingViewCommand -> {
                val swapCoinProcessingDialog: SwapCoinProcessingDialog =
                    SwapCoinProcessingDialog.newInstance() {
                        // viewModel.openDoneDialog(command.)
                    }
                swapCoinProcessingDialog.show(
                    childFragmentManager,
                    SwapCoinProcessingDialog.SWAP_COIN_PROGRESS
                )
            }
            is Command.SendCoinDoneViewCommand -> {
                val sendCoinDone: SendCoinDoneDialog =
                    SendCoinDoneDialog.newInstance(
                        command.transactionInfo
                    ) {
                        dismiss()
                    }

                sendCoinDone.show(childFragmentManager, SendCoinDoneDialog.SEND_COIN_DONE)
            }
        }
    }

    private fun initObserves() {
        viewModel.command.observe(viewLifecycleOwner) {
            processViewCommand(it)
        }
        viewModel.selectedWalletFrom.observe(viewLifecycleOwner) {
            selectedWalletFrom = it
            updateData()
        }
        viewModel.selectedWalletTo.observe(viewLifecycleOwner) {
            viewModel.makeDialogFullScreen()
            viewModel.setAmountOfConvertingToken(binding.edtAmountFrom.text.toString())
            updateData()
        }
        viewModel.selectedWalletTo.observe(viewLifecycleOwner) {
            binding.swapButton.isEnabled = it != null
        }
        viewModel.amount.observe(viewLifecycleOwner) {
            viewModel.setAroundToCurrency(it)
            viewModel.setAmountOfConvertingToken(it)
        }
        viewModel.makeDialogFullScreen.observe(viewLifecycleOwner) {
            dialog?.makeFullScreen(binding.root, this)
        }
        viewModel.insufficientFoundsError.observe(viewLifecycleOwner) {
            toast("Insufficient founds")
        }
    }
}