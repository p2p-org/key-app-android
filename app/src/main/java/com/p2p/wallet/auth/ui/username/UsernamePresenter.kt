package com.p2p.wallet.auth.ui.username

import com.p2p.wallet.auth.interactor.UsernameInteractor
import com.p2p.wallet.common.mvp.BasePresenter

class UsernamePresenter(
    private val interactor: UsernameInteractor
) :
    BasePresenter<UsernameContract.View>(),
    UsernameContract.Presenter