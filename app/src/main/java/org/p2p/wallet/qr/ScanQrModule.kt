package org.p2p.wallet.qr

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.qr.interactor.QrCodeInteractor
import org.p2p.wallet.qr.model.QrParams

object ScanQrModule : InjectionModule {

    override fun create() = module {
        factory { QrCodeInteractor(get()) }
        single {
            val context = androidContext()
            QrParams(
                contentColor = context.getColor(R.color.night),
                backgroundColor = context.getColor(R.color.smoke)
            )
        }
    }
}
