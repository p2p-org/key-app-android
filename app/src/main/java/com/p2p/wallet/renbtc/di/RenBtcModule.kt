package com.p2p.wallet.renbtc.di

import android.content.Context
import com.google.gson.Gson
import com.p2p.wallet.BuildConfig
import com.p2p.wallet.R
import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.infrastructure.network.NetworkModule.createLoggingInterceptor
import com.p2p.wallet.renbtc.api.RenBTCApi
import com.p2p.wallet.renbtc.interactor.BurnBtcInteractor
import com.p2p.wallet.renbtc.interactor.RenBtcInteractor
import com.p2p.wallet.renbtc.repository.RenBTCRemoteRepository
import com.p2p.wallet.renbtc.repository.RenBTCRepository
import com.p2p.wallet.renbtc.ui.main.RenBTCContract
import com.p2p.wallet.renbtc.ui.main.RenBTCPresenter
import com.p2p.wallet.renbtc.ui.statuses.ReceivingStatusesContract
import com.p2p.wallet.renbtc.ui.statuses.ReceivingStatusesPresenter
import okhttp3.OkHttpClient
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RenBtcModule : InjectionModule {

    override fun create() = module {
        single {
            val client = OkHttpClient.Builder()
                .apply { if (BuildConfig.DEBUG) addInterceptor(createLoggingInterceptor("Blockstream")) }
                .build()
            val api = Retrofit.Builder()
                .baseUrl(get<Context>().getString(R.string.blockstreamUrl))
                .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
                .client(client)
                .build()
                .create(RenBTCApi::class.java)

            RenBTCRemoteRepository(api, get())
        } bind RenBTCRepository::class

        single { RenBtcInteractor(get(), get(), get()) }
        single { BurnBtcInteractor(get(), get(), get()) }

        factory { RenBTCPresenter(get(), get()) } bind RenBTCContract.Presenter::class
        factory { ReceivingStatusesPresenter(get()) } bind ReceivingStatusesContract.Presenter::class
    }
}