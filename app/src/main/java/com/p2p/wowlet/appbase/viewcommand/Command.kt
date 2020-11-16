package com.p2p.wowlet.appbase.viewcommand

sealed class Command {

    class NetworkErrorViewCommand : ViewCommand
    /*Navigate fragment commands*/
    class FinishAppViewCommand : ViewCommand
    class NavigateUpViewCommand(val destinationId: Int) : ViewCommand
    class NavigateRegLoginViewCommand(val destinationId: Int) : ViewCommand
    class NavigatePinCodeViewCommand(val destinationId: Int) : ViewCommand
    class NavigateRegWalletViewCommand(val destinationId: Int) : ViewCommand
    class NavigateRegFinishViewCommand(val destinationId: Int) : ViewCommand
    class NavigateNotificationViewCommand(val destinationId: Int) : ViewCommand
    class NavigateFaceIdViewCommand(val destinationId: Int) : ViewCommand
    class NavigateRecoveryWalletViewCommand(val destinationId:Int) : ViewCommand
    class NavigateSecretKeyViewCommand(val destinationId: Int) : ViewCommand
    class NavigateSwapViewCommand(val destinationId: Int) : ViewCommand
    class NavigateCompleteBackupViewCommand(val destinationId: Int) : ViewCommand
    class NavigateScannerViewCommand(val destinationId: Int) : ViewCommand
    class OpenAddCoinDialogViewCommand() : ViewCommand
    class NavigateReceiveViewCommand(val destinationId: Int) : ViewCommand
    class NavigateSendCoinViewCommand(val destinationId: Int) : ViewCommand
    class NavigateDetailSavingViewCommand(val destinationId: Int) : ViewCommand
    class OpenProfileDialogViewCommand() : ViewCommand
    class OpenMainActivityViewCommand() : ViewCommand


    class EnterWalletDialogViewCommand() : ViewCommand
    class MyWalletDialogViewCommand() : ViewCommand
    class SendCoinDoneViewCommand() : ViewCommand
    class SwapCoinProcessingViewCommand() : ViewCommand
}