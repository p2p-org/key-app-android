package org.p2p.wallet.common.mvp

interface MvpPresenter<V : MvpView> {

    fun attach(view: V)

    fun detach()
}
