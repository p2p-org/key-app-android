package org.p2p.wallet.common.mvp

/**
 * @see BasePresenter
 * @see BaseFragment
 */
interface MvpPresenter<V : MvpView> {
    /**
     * called on each `onViewCreated` from View
     */
    fun attach(view: V)

    fun detach()
}
