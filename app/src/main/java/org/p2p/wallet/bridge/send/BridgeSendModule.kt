package org.p2p.wallet.bridge.send

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.bridge.send.ui.BridgeSendContract
import org.p2p.wallet.bridge.send.ui.BridgeSendPresenter
import org.p2p.wallet.common.di.InjectionModule

object BridgeSendModule : InjectionModule {
    override fun create() = module {
        factoryOf(::SendFragmentFactory)
        factoryOf(::BridgeSendPresenter) bind BridgeSendContract.Presenter::class
    }
}
