package com.p2p.wallet.main

import android.content.Context
import com.google.gson.Gson
import com.p2p.wallet.BuildConfig
import com.p2p.wallet.R
import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.main.api.RenBTCApi
import com.p2p.wallet.main.interactor.MainInteractor
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.repository.MainDatabaseRepository
import com.p2p.wallet.main.repository.MainLocalRepository
import com.p2p.wallet.main.repository.RenBTCRemoteRepository
import com.p2p.wallet.main.repository.RenBTCRepository
import com.p2p.wallet.main.ui.buy.BuyContract
import com.p2p.wallet.main.ui.buy.BuyPresenter
import com.p2p.wallet.main.ui.main.MainContract
import com.p2p.wallet.main.ui.main.MainPresenter
import com.p2p.wallet.main.ui.receive.solana.ReceivePresenter
import com.p2p.wallet.main.ui.receive.solana.ReceiveSolanaContract
import com.p2p.wallet.main.ui.send.SendContract
import com.p2p.wallet.main.ui.send.SendPresenter
import com.p2p.wallet.renBTC.interactor.RenBTCInteractor
import com.p2p.wallet.renBTC.renbtc.RenBTCContract
import com.p2p.wallet.renBTC.renbtc.RenBTCPresenter
import com.p2p.wallet.renBTC.statuses.ReceivingStatusesContract
import com.p2p.wallet.renBTC.statuses.ReceivingStatusesPresenter
import com.p2p.wallet.user.UserModule.createLoggingInterceptor
import okhttp3.OkHttpClient
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MainModule : InjectionModule {

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

        single { RenBTCInteractor(get(), get(), get()) }

        factory { MainDatabaseRepository(get()) } bind MainLocalRepository::class

        /* Cached data exists, therefore creating singleton */
        single { MainPresenter(get(), get()) } bind MainContract.Presenter::class
        factory { MainInteractor(get(), get(), get()) }

        factory { (token: Token?) -> ReceivePresenter(token, get(), get()) } bind ReceiveSolanaContract.Presenter::class
        factory { (token: Token) -> SendPresenter(token, get(), get()) } bind SendContract.Presenter::class
        factory { (token: Token?) -> BuyPresenter(token, get(), get()) } bind BuyContract.Presenter::class
        factory { RenBTCPresenter(get(), get()) } bind RenBTCContract.Presenter::class
        factory { ReceivingStatusesPresenter(get()) } bind ReceivingStatusesContract.Presenter::class
    }
}