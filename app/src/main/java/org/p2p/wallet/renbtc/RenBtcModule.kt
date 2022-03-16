package org.p2p.wallet.renbtc

import android.content.Context
import okhttp3.OkHttpClient
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.network.NetworkModule.httpLoggingInterceptor
import org.p2p.wallet.renbtc.api.RenBTCApi
import org.p2p.wallet.renbtc.interactor.BurnBtcInteractor
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.renbtc.repository.RenDatabaseRepository
import org.p2p.wallet.renbtc.repository.RenLoaclRepository
import org.p2p.wallet.renbtc.repository.RenRemoteRepository
import org.p2p.wallet.renbtc.repository.RenRepository
import org.p2p.wallet.renbtc.repository.RenStateInMemoryRepository
import org.p2p.wallet.renbtc.repository.RenStateLocalRepository
import org.p2p.wallet.renbtc.ui.main.RenBTCContract
import org.p2p.wallet.renbtc.ui.main.RenBTCPresenter
import org.p2p.wallet.renbtc.ui.status.RenStatusesContract
import org.p2p.wallet.renbtc.ui.status.RenStatusesPresenter
import org.p2p.wallet.renbtc.ui.transactions.RenTransactionsContract
import org.p2p.wallet.renbtc.ui.transactions.RenTransactionsPresenter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RenBtcModule : InjectionModule {

    override fun create() = module {
        single {
            val client = OkHttpClient.Builder()
                .apply { if (BuildConfig.DEBUG) addInterceptor(httpLoggingInterceptor("Blockstream")) }
                .build()
            val api = Retrofit.Builder()
                .baseUrl(get<Context>().getString(R.string.blockstreamUrl))
                .addConverterFactory(GsonConverterFactory.create(get()))
                .client(client)
                .build()
                .create(RenBTCApi::class.java)

            RenRemoteRepository(api)
        } bind RenRepository::class
        single { RenDatabaseRepository(get()) } bind RenLoaclRepository::class
        single { RenStateInMemoryRepository() } bind RenStateLocalRepository::class
        single { RenBtcInteractor(get(), get(), get(), get(), get()) }

        single { RenTransactionManager(get(), get()) }
        single { BurnBtcInteractor(get(), get(), get()) }

        factory { RenBTCPresenter(get(), get(), get(), get(), get()) } bind RenBTCContract.Presenter::class
        factory { RenTransactionsPresenter(get(), get()) } bind RenTransactionsContract.Presenter::class
        factory { RenStatusesPresenter(get()) } bind RenStatusesContract.Presenter::class
    }
}
