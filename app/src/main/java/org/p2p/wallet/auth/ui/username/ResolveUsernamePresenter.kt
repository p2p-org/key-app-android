package org.p2p.wallet.auth.ui.username

import org.p2p.wallet.common.mvp.BasePresenter

import kotlinx.coroutines.Job

class ResolveUsernamePresenter() : BasePresenter<ResolveUsernameContract.View>(), ResolveUsernameContract.Presenter {

    private var job: Job? = null
}