package com.p2p.wallet.qr

import android.content.Context
import androidx.core.content.ContextCompat
import com.p2p.wallet.R
import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.qr.interactor.QrCodeInteractor
import com.p2p.wallet.qr.model.QrColors
import org.koin.dsl.module

object QrModule : InjectionModule {

    override fun create() = module {
        factory { QrCodeInteractor() }

        single {
            val context = get<Context>()
            QrColors(
                ContextCompat.getColor(context, R.color.colorDark),
                ContextCompat.getColor(context, R.color.colorGraySecondary)
            )
        }
    }
}