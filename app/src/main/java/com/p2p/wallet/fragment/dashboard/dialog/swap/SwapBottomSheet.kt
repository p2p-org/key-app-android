package com.p2p.wowlet.fragment.dashboard.dialog.swap

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.p2p.wallet.fragment.dashboard.dialog.swap.viewmodel.SwapViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SwapBottomSheet(
    private val navigateToFragment: (destinationId: Int, bundle: Bundle?) -> Unit
) : BottomSheetDialogFragment() {

    private val viewModel: SwapViewModel by viewModel()

    companion object {
        const val TAG_SWAP_BOTTOM_SHEET = "swapBottomSheet"
//        fun newInstance(
//            allMyWallets: List<WalletItem>,
//            selectedWalletItems: WalletItem,
//            navigateToFragment: (destinationId: Int, bundle: Bundle?) -> Unit
//        ): SwapBottomSheet {
//            return SwapBottomSheet(allMyWallets, selectedWalletItems, navigateToFragment)
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        initObserves()
//        viewModel.setSelectedWalletFrom(selectedWalletFrom)
//        binding.run {
//            viewModel = this@SwapBottomSheet.viewModel
//            lifecycleOwner = this@SwapBottomSheet
//            walletItemFrom = selectedWalletFrom
//        }
    }

//    @SuppressLint("SetTextI18n")
//    private fun processViewCommand(command: ViewCommand) {
//        when (command) {
//            is NavigateUpViewCommand -> navigateToFragment.invoke(command.destinationId, null)
//            is NavigateUpBackStackCommand -> dismiss()
//            is OpenMyWalletDialogViewCommand -> {
//                YourWalletsBottomSheet.newInstance(
//                    getString(R.string.select_source)
//                ) {
//                    viewModel.setSelectedWalletFrom(it)
//                }.show(
//                    childFragmentManager,
//                    YourWalletsBottomSheet.YOUR_WALLET
//                )
//            }
//            is OpenSelectTokenToSwapBottomSheet -> {
//                SelectTokenToSwapBottomSheet.newInstance(
//                    selectedWalletFrom,
//                    allMyWallets
//                ) {
//                    viewModel.setSelectedWalletTo(it)
//                }.show(
//                    childFragmentManager,
//                    SelectTokenToSwapBottomSheet.TAG_SELECT_TOKEN_TO_SWAP
//                )
//            }
//            is OpenSlippageSettingsBottomSheet -> {
//                SlippageSettingsBottomSheet.newInstance {
//                    binding.slippagePercent.text = "$it%"
//                    viewModel.setSlippage(it)
//                    makeShortToast("Slippage is $it%")
//                    dialog?.makeFullScreen(binding.root, this)
//                }.show(
//                    childFragmentManager,
//                    SlippageSettingsBottomSheet.TAG_SLIPPAGE_SETTINGS
//                )
//            }
//            is SwapCoinProcessingViewCommand -> {
//                val swapCoinProcessingDialog: SwapCoinProcessingDialog =
//                    SwapCoinProcessingDialog.newInstance() {
//                        //viewModel.openDoneDialog(command.)
//                    }
//                swapCoinProcessingDialog.show(
//                    childFragmentManager,
//                    SwapCoinProcessingDialog.SWAP_COIN_PROGRESS
//                )
//            }
//            is SendCoinDoneViewCommand -> {
//                val sendCoinDone: SendCoinDoneDialog =
//                    SendCoinDoneDialog.newInstance(
//                        command.transactionInfo,
//                        {
//                            dismiss()
//                        },
//                        { destinationId, bundle ->
//                            //Navigate to block chain explorer
//                            navigateToFragment.invoke(destinationId, bundle)
//                        }
//                    )
//
//                sendCoinDone.show(childFragmentManager, SendCoinDoneDialog.SEND_COIN_DONE)
//            }
//        }
//    }

//    private fun initObserves() {
//        viewModel.command.observe(viewLifecycleOwner) {
//            processViewCommand(it)
//        }
//        viewModel.selectedWalletFrom.observe(viewLifecycleOwner) {
//            binding.walletItemFrom = it
//            selectedWalletFrom = it
//        }
//        viewModel.selectedWalletTo.observe(viewLifecycleOwner) {
//            binding.walletItemTo = it
//            viewModel.makeDialogFullScreen()
//            viewModel.setAmountOfConvertingToken(binding.edtAmountFrom.text.toString())
//        }
//        viewModel.amount.observe(viewLifecycleOwner) {
//            viewModel.setAroundToCurrency(it)
//            viewModel.setAmountOfConvertingToken(it)
//        }
//        viewModel.makeDialogFullScreen.observe(viewLifecycleOwner) {
//            dialog?.makeFullScreen(binding.root, this)
//        }
//        viewModel.insufficientFoundsError.observe(viewLifecycleOwner) {
//            makeShortToast("Insufficient founds")
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }

}