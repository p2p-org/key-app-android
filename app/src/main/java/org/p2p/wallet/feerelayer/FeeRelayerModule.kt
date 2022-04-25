package org.p2p.wallet.feerelayer

import android.content.Context
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.R
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.feerelayer.api.FeeRelayerApi
import org.p2p.wallet.feerelayer.api.FeeRelayerDevnetApi
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInstructionsInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerSwapInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerTopUpInteractor
import org.p2p.wallet.feerelayer.repository.FeeRelayerRemoteRepository
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import org.p2p.wallet.infrastructure.network.feerelayer.FeeRelayerInterceptor
import retrofit2.Retrofit

object FeeRelayerModule : InjectionModule {

    const val FEE_RELAYER_QUALIFIER = "https://fee-relayer.solana.p2p.org"
    override fun create() = module {
        single(named(FEE_RELAYER_QUALIFIER)) {
            val baseUrl = get<Context>().getString(R.string.feeRelayerBaseUrl)
            getRetrofit(baseUrl, "FeeRelayer", interceptor = FeeRelayerInterceptor(get()))
        }

        single {
            val retrofit = get<Retrofit>(named(FEE_RELAYER_QUALIFIER))
            val api = retrofit.create(FeeRelayerApi::class.java)
            val devnetApi = retrofit.create(FeeRelayerDevnetApi::class.java)
            FeeRelayerRemoteRepository(api, devnetApi, get())
        } bind FeeRelayerRepository::class

        single {
            FeeRelayerAccountInteractor(
                userAccountRepository = get(),
                amountRepository = get(),
                userInteractor = get(),
                feeRelayerRepository = get(),
                tokenKeyProvider = get()
            )
        }

        factory { FeeRelayerInteractor(get(), get(), get(), get(), get(), get()) }
        factory { FeeRelayerTopUpInteractor(get(), get(), get(), get(), get(), get()) }
        factory { FeeRelayerInstructionsInteractor(get(), get(), get(), get()) }
        single { FeeRelayerSwapInteractor(get(), get(), get(), get(), get(), get(), get()) }
    }
}
