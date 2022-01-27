package org.p2p.wallet.qr

import android.content.Context
import androidx.core.content.ContextCompat
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.qr.interactor.QrCodeInteractor
import org.p2p.wallet.qr.model.QrParams
import org.koin.dsl.module

object QrModule : InjectionModule {

    override fun create() = module {
        factory { QrCodeInteractor(get()) }
        single {
            val context = get<Context>()
            QrParams(
                ContextCompat.getColor(context, R.color.chartMATH),
                ContextCompat.getColor(context, R.color.white)
            )
        }
    }
}