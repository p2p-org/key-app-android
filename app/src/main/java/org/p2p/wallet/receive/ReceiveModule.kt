package org.p2p.wallet.receive

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.receive.eth.ReceiveEthereumContract
import org.p2p.wallet.receive.eth.ReceiveEthereumPresenter
import org.p2p.wallet.receive.solana.NewReceiveSolanaContract
import org.p2p.wallet.receive.solana.NewReceiveSolanaPresenter
import org.p2p.wallet.receive.tokenselect.ReceiveTokensContract
import org.p2p.wallet.receive.tokenselect.ReceiveTokensPresenter

object ReceiveModule : InjectionModule {

    override fun create() = module {
        factoryOf(::ReceiveFragmentFactory)
        factoryOf(::ReceiveTokensPresenter) bind ReceiveTokensContract.Presenter::class
        factoryOf(::NewReceiveSolanaPresenter) bind NewReceiveSolanaContract.Presenter::class
        factoryOf(::ReceiveEthereumPresenter) bind ReceiveEthereumContract.Presenter::class
    }
}
