package com.p2p.wowlet.fragment.reglogin.viewmodel
import com.p2p.wowlet.R
import com.p2p.wowlet.appbase.viewcommand.Command
import com.p2p.wowlet.appbase.viewmodel.BaseViewModel
import com.wowlet.domain.interactors.RegLoginInteractor

class RegLoginViewModel(val regLoginInteractor: RegLoginInteractor) : BaseViewModel() {


    fun goToRecoveryWalletFragment() {
        _command.value =
            Command.NavigateRecoveryWalletViewCommand(R.id.action_navigation_reg_login_to_navigation_recovery_wallet)
    }

    fun goToTermAndConditionFragment() {
        _command.value =
            Command.NavigateTermAndConditionViewCommand(R.id.action_navigation_reg_login_to_navigation_term_and_condition)
    }

    fun finishApp() {
        _command.value = Command.FinishAppViewCommand()
    }

}