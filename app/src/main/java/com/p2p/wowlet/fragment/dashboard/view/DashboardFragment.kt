package com.p2p.wowlet.fragment.dashboard.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import com.p2p.wowlet.R
import com.p2p.wowlet.activity.MainActivity
import com.p2p.wowlet.activity.RegistrationActivity
import com.p2p.wowlet.appbase.FragmentBaseMVVM
import com.p2p.wowlet.appbase.utils.dataBinding
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewcommand.Command.*
import com.p2p.wowlet.appbase.viewcommand.ViewCommand
import com.p2p.wowlet.databinding.FragmentDashboardBinding
import com.p2p.wowlet.fragment.dashboard.dialog.BackupDialog
import com.p2p.wowlet.fragment.dashboard.dialog.currency.CurrencyDialog
import com.p2p.wowlet.fragment.dashboard.dialog.ProfileDetailsDialog
import com.p2p.wowlet.fragment.dashboard.dialog.SecurityDialog
import com.p2p.wowlet.fragment.dashboard.dialog.addcoin.AddCoinBottomSheet
import com.p2p.wowlet.fragment.dashboard.dialog.addcoin.AddCoinBottomSheet.Companion.TAG_ADD_COIN
import com.p2p.wowlet.fragment.dashboard.dialog.allmytokens.AllMyTokensBottomSheet
import com.p2p.wowlet.fragment.dashboard.dialog.allmytokens.AllMyTokensBottomSheet.Companion.TAG_ALL_MY_TOKENS_DIALOG
import com.p2p.wowlet.fragment.dashboard.dialog.detailwallet.DetailWalletBottomSheet
import com.p2p.wowlet.fragment.dashboard.dialog.detailwallet.DetailWalletBottomSheet.Companion.DETAIL_WALLET
import com.p2p.wowlet.fragment.dashboard.dialog.enterwallet.EnterWalletBottomSheet
import com.p2p.wowlet.fragment.dashboard.dialog.enterwallet.EnterWalletBottomSheet.Companion.ENTER_WALLET
import com.p2p.wowlet.fragment.dashboard.dialog.networks.NetworksDialog
import com.p2p.wowlet.fragment.dashboard.dialog.profile.ProfileDialog
import com.p2p.wowlet.fragment.dashboard.dialog.recoveryphrase.RecoveryPhraseDialog
import com.p2p.wowlet.fragment.dashboard.dialog.savedcards.SavedCardsDialog
import com.p2p.wowlet.fragment.dashboard.dialog.sendyourwallet.YourWalletBottomSheet
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.p2p.wowlet.dialog.sendcoins.view.SendCoinsBottomSheet
import com.p2p.wowlet.dialog.sendcoins.view.SendCoinsBottomSheet.Companion.TAG_SEND_COIN
import com.p2p.wowlet.fragment.dashboard.dialog.TransactionBottomSheet
import com.p2p.wowlet.fragment.dashboard.dialog.addcoin.util.pxToDp
import com.p2p.wowlet.fragment.dashboard.dialog.swap.SwapBottomSheet
import com.p2p.wowlet.utils.OnSwipeTouchListener
import com.p2p.wowlet.utils.drawChart
import org.koin.android.ext.android.inject
import com.wowlet.entities.local.WalletItem
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.p2p.solanaj.rpc.RpcClient

class DashboardFragment : FragmentBaseMVVM<DashboardViewModel, FragmentDashboardBinding>() {

    override val viewModel: DashboardViewModel by viewModel()
    override val binding: FragmentDashboardBinding by dataBinding(R.layout.fragment_dashboard)

    private val rpcClient: RpcClient by inject()


    private var isBackupSuccess: Boolean? = null

    companion object {
        const val BACK_TO_DASHBOARD_SCREEN = "backToDashboardScreen"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            viewModel = this@DashboardFragment.viewModel
        }
        viewModel.getWalletItems()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        if (isBackupSuccess != null) {
            if (isBackupSuccess as Boolean) {
                viewModel.openRecoveryPhraseDialog()
            } else {
                viewModel.openBackupFailedDialog()
            }
        }

        binding.rootContainer.setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeRight() {
                viewModel.goToScannerFragment()
            }
        })
        binding.vRvWallets.setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeRight() {
                viewModel.goToScannerFragment()
            }
        })
    }

    override fun initData() {
        arguments?.let {
            isBackupSuccess = it.getBoolean(BACK_TO_DASHBOARD_SCREEN)
            it.clear()
        }
    }

    override fun processViewCommand(command: ViewCommand) {
        when (command) {
            is NavigateUpViewCommand -> navigateFragment(command.destinationId)
            is NavigateScannerViewCommand -> navigateFragment(command.destinationId)
            is OpenSwapBottomSheetViewCommand -> {
                SwapBottomSheet.newInstance(
                    allMyWallets = command.allMyWallets,
                    selectedWalletItems = command.walletData,
                    navigateToFragment = { destinationId, bundle ->
                        navigateFragment(destinationId, bundle)
                    }
                ).show(
                    childFragmentManager,
                    SwapBottomSheet.TAG_SWAP_BOTTOM_SHEET
                )
            }
            is OpenWalletDetailDialogViewCommand -> {
                DetailWalletBottomSheet.newInstance(command.walletItem, {
                    viewModel.goToQrScanner(it)
                }, {
                    viewModel.openAddCoinDialog()
                }, {
                    viewModel.goToSendCoin(it)
                }, {
                    viewModel.enterWalletDialog()
                }, {
                    viewModel.openSwapBottomSheet(it)
                }, { destinationId, bundle ->
                    navigateFragment(destinationId, bundle)
                }).show(
                    childFragmentManager,
                    DETAIL_WALLET
                )
            }
            is OpenSendCoinDialogViewCommand -> {
//                val walletItem =
//                    command.bundle?.getParcelable<WalletItem>(SendCoinsBottomSheet.WALLET_ITEM)
//                val walletAddress =
//                    command.bundle?.getString(SendCoinsBottomSheet.WALLET_ADDRESS, "") ?: ""
                SendCoinsBottomSheet.newInstance(
                    command.walletItem, command.walletAddress
                ) { destinationId, bundle ->
                    navigateFragment(destinationId, bundle)
                }.show(
                    childFragmentManager,
                    TAG_SEND_COIN
                )
            }


            is NavigateDetailSavingViewCommand -> {
                navigateFragment(command.destinationId)
                (activity as MainActivity).showHideNav(false)
            }


            is NavigateBlockChainViewCommand -> {
                navigateFragment(command.destinationId, command.bundle)
            }
            is OpenAddCoinDialogViewCommand -> {
                AddCoinBottomSheet.newInstance(
                    goToDetailWalletFragment = {
                        viewModel.goToDetailWalletFragment(it)
                        viewModel.getWalletItems()
                    },
                    goToSolanaExplorerFragment = {
                        viewModel.goToBlockChainExplorer(it)
                    },
                    updateListInAllMyTokens =
                    command.updateAllMyTokens

                ).show(childFragmentManager, TAG_ADD_COIN)
            }

            is Command.OpenTransactionDialogViewCommand -> {
                TransactionBottomSheet.newInstance(command.itemActivity) { destinationId, bundle ->
                    navigateFragment(destinationId, bundle)
                }.show(childFragmentManager, TransactionBottomSheet.TRANSACTION_DIALOG)
            }
            is OpenProfileDetailDialogViewCommand -> {
                ProfileDetailsDialog.newInstance {
                    viewModel.clearSecretKey()
                    viewModel.clearFingerprint()
                    activity?.let {
                        val intent = Intent(it, RegistrationActivity::class.java)
                        it.startActivity(intent)
                        it.finish()
                    }
                }.show(
                    childFragmentManager,
                    ProfileDetailsDialog.TAG_PROFILE_DETAILS_DIALOG
                )
            }
            is OpenBackupDialogViewCommand -> {
                BackupDialog.newInstance {
                    viewModel.goToPinCodeFragment()
                }.show(childFragmentManager, BackupDialog.TAG_BACKUP_DIALOG)
            }
            is NavigatePinCodeViewCommand -> {
                navigateFragment(command.destinationId, command.bundle)
            }
            is OpenCurrencyDialogViewCommand -> {
                CurrencyDialog.newInstance(command.onCurrencySelected)
                    .show(childFragmentManager, CurrencyDialog.TAG_CURRENCY_DIALOG)
            }
            is OpenSavedCardDialogViewCommand -> {
                SavedCardsDialog.newInstance()
                    .show(childFragmentManager, SavedCardsDialog.TAG_SAVED_CARDS_DIALOG)
            }
            is OpenSecurityDialogViewCommand -> {
                SecurityDialog.newInstance(command.onFingerprintStateSelected)
                    .show(childFragmentManager, SecurityDialog.TAG_SECURITY_DIALOG)
            }
            is OpenNetworkDialogViewCommand -> {
                NetworksDialog.newInstance {
                    rpcClient.updateEndpoint(it.endpoint)
                    viewModel.getWalletItems()
                    command.onNetworkSelected.invoke()
                }.show(childFragmentManager, NetworksDialog.TAG_NETWORKS_DIALOG)
            }
            is OpenProfileDialogViewCommand -> {
                ProfileDialog.newInstance({
                    viewModel.goToProfileDetailDialog()
                }, {
                    viewModel.goToBackupDialog()
                }, {
                    viewModel.goToNetworkDialog(it)
                }, {
                    viewModel.goToCurrencyDialog(it)
                }, {
                    viewModel.goToSavedCardDialog()
                },
                    {
                        viewModel.goToSecurityCardDialog(it)
                    }).show(
                    childFragmentManager,
                    ProfileDialog.TAG_PROFILE_DIALOG
                )
            }
            is OpenAllMyTokensDialogViewCommand -> {
                AllMyTokensBottomSheet.newInstance(command.yourWallets, {
                    viewModel.openAddCoinDialog()
                }) { itemWallet ->
                    viewModel.goToDetailWalletFragment(itemWallet)
                }.show(
                    childFragmentManager, TAG_ALL_MY_TOKENS_DIALOG
                )
            }
            is OpenRecoveryPhraseDialogViewCommand -> {
                RecoveryPhraseDialog.newInstance()
                    .show(childFragmentManager, RecoveryPhraseDialog.TAG_RECOVERY_DIALOG)
            }
            is OpenBackupFailedDialogViewCommand -> {

            }
            is YourWalletDialogViewCommand -> {
                YourWalletBottomSheet.newInstance(command.enterWallet).show(
                    childFragmentManager,
                    YourWalletBottomSheet.ENTER_YOUR_WALLET
                )
            }
            is EnterWalletDialogViewCommand -> {
                EnterWalletBottomSheet.newInstance(command.list).show(
                    childFragmentManager,
                    ENTER_WALLET
                )
            }
        }
    }

    override fun observes() {
        observe(viewModel.getWalletDataError) {
            context?.run {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
        observe(viewModel.getWalletChart) {
            binding.vPieChartData.drawChart(it)
        }
        observe(viewModel.getAllWalletData) { itemList ->
            when {
                itemList.isEmpty() -> {
                    binding.allMyTokensContainer.visibility = View.GONE
                    binding.addTokensContainer.visibility = View.GONE
                }
                itemList.size <= 4 -> {
                    binding.allMyTokensContainer.visibility = View.GONE
                    binding.addTokensContainer.visibility = View.VISIBLE
                }
                else -> {
                    binding.allMyTokensContainer.visibility = View.VISIBLE
                    binding.addTokensContainer.visibility = View.GONE
                }
            }
            binding.rootContainer.post {
                val myBalanceContainer: Int =
                    binding.myBalanceContainer.run { height + marginTop + marginBottom }
                val lCoinContainer: Int =
                    binding.lCoinContainer.run { height +  marginBottom }
                val rvMargins: Int = binding.vRvWallets.run { marginTop + marginBottom }
                val button: Int = binding.run {
                    addTokensContainer.run { height + marginBottom + marginTop }
                    +allMyTokensContainer.run { height + marginBottom + marginTop }
                }
                val allowedHeight =
                    binding.rootContainer.height - (myBalanceContainer + lCoinContainer + rvMargins + button)
                val layoutParams: ViewGroup.LayoutParams =
                    if (binding.vRvWallets.height > allowedHeight) {
                        binding.vRvWallets.layoutParams.apply {
                            height = allowedHeight
                        }
                    } else {
                        binding.vRvWallets.layoutParams.apply {
                            height = ViewGroup.LayoutParams.WRAP_CONTENT
                        }
                    }
                binding.vRvWallets.layoutParams = layoutParams
            }
        }
    }

    override fun navigateUp() {
        viewModel.finishApp()
    }
}