package org.p2p.wallet.qr

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.p2p.wallet.R
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.qr.interactor.QrCodeInteractor
import org.p2p.wallet.qr.model.QrParams

object ScanQrModule : InjectionModule {

    override fun create() = module {
        factory { QrCodeInteractor(get()) }
        single {
            val context = androidContext()
            QrParams(
                contentColor = context.getColor(R.color.bg_night),
                backgroundColor = context.getColor(R.color.bg_smoke)
            )
        }
    }
}
