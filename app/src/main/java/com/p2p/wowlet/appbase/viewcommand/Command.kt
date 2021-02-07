package com.p2p.wowlet.appbase.viewcommand

import android.os.Bundle
import com.wowlet.entities.local.ActivityItem
import com.wowlet.entities.local.EnterWallet
import com.wowlet.entities.local.WalletItem
import com.wowlet.entities.local.YourWallets
import org.bitcoinj.wallet.Wallet

sealed class Command {

    class NetworkErrorViewCommand : ViewCommand

    /*Navigate fragment commands*/
    class FinishAppViewCommand : ViewCommand
    class NavigateUpViewCommand(val destinationId: Int) : ViewCommand
    class NavigateUpBackStackCommand() : ViewCommand
    class NavigateRegLoginViewCommand(val destinationId: Int) : ViewCommand
    class NavigateTermAndConditionViewCommand(val destinationId: Int) : ViewCommand
    class NavigatePinCodeViewCommand(val destinationId: Int, val bundle: Bundle?) : ViewCommand

    // class NavigatePinCodeVerificationViewCommand(val destinationId: Int) : ViewCommand
    class NavigateRegWalletViewCommand(val destinationId: Int) : ViewCommand
    class NavigateCreateWalletViewCommand(val destinationId: Int) : ViewCommand
    class NavigateRegFinishViewCommand(val destinationId: Int) : ViewCommand
    class NavigateNotificationViewCommand(val destinationId: Int) : ViewCommand
    class NavigateFingerPrintViewCommand(val destinationId: Int) : ViewCommand
    class NavigateRecoveryWalletViewCommand(val destinationId: Int) : ViewCommand
    class NavigateSecretKeyViewCommand(val destinationId: Int) : ViewCommand

    class NavigateScannerViewCommand(val destinationId: Int, val bundle: Bundle? = null) : ViewCommand
    class NavigateScannerFromSendCoinViewCommand() : ViewCommand
    class OpenAddCoinDialogViewCommand(val updateAllMyTokens: () -> Unit) : ViewCommand
    class NavigateWalletViewCommand(val destinationId: Int, val bundle: Bundle?) : ViewCommand
    class OpenSendCoinDialogViewCommand(val bundle: Bundle? = null) : ViewCommand
    class NavigateBlockChainViewCommand(val destinationId: Int, val bundle: Bundle?) : ViewCommand
    class NavigateDashboardViewCommand(val destinationId: Int, val bundle: Bundle?) :
        ViewCommand

    class NavigateDetailSavingViewCommand(val destinationId: Int) : ViewCommand
    class OpenProfileDialogViewCommand() : ViewCommand
    class OpenMainActivityViewCommand() : ViewCommand

    class OpenSwapBottomSheetViewCommand(val walletData: WalletItem, val allMyWallets: List<WalletItem>) : ViewCommand
    class OpenSelectTokenToSwapBottomSheet() : ViewCommand
    class OpenSlippageSettingsBottomSheet() : ViewCommand


    class EnterWalletDialogViewCommand(val list: List<EnterWallet>) : ViewCommand
    class YourWalletDialogViewCommand(val enterWallet: EnterWallet) : ViewCommand
    class OpenMyWalletDialogViewCommand() : ViewCommand
    class OpenProfileDetailDialogViewCommand() : ViewCommand
    class OpenBackupDialogViewCommand() : ViewCommand
    class OpenBackupFailedDialogViewCommand() : ViewCommand
    class OpenCurrencyDialogViewCommand(val onCurrencySelected: () -> Unit) : ViewCommand
    class OpenSavedCardDialogViewCommand() : ViewCommand
    class OpenSecurityDialogViewCommand(val onFingerprintStateSelected: () -> Unit) : ViewCommand
    class OpenNetworkDialogViewCommand(val onNetworkSelected: () -> Unit) : ViewCommand
    class OpenRecoveryPhraseDialogViewCommand() : ViewCommand
    class OpenTransactionDialogViewCommand(val itemActivity: ActivityItem) : ViewCommand
    class OpenWalletDetailDialogViewCommand(val walletItem: WalletItem) : ViewCommand
    class OpenEditWalletDialogViewCommand(val walletItem: WalletItem) : ViewCommand
    class SendCoinDoneViewCommand(val transactionInfo: ActivityItem) : ViewCommand
    class SendCoinViewCommand() : ViewCommand
    class SwapCoinProcessingViewCommand() : ViewCommand
    class OpenAllMyTokensDialogViewCommand(val yourWallets: YourWallets) : ViewCommand



}