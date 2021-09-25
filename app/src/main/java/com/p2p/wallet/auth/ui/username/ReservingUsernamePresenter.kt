package com.p2p.wallet.auth.ui.username

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.restore.interactor.SecretKeyInteractor
import kotlinx.coroutines.launch
import org.p2p.solanaj.crypto.DerivationPath
import kotlin.properties.Delegates

class ReservingUsernamePresenter(

) : BasePresenter<ReservingUsernameContract.View>(), ReservingUsernameContract.Presenter {

    override fun checkUsername() {
        launch {

        }
    }

    override fun createUsername() {
        launch {

        }
    }

}