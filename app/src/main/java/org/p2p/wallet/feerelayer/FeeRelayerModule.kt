package org.p2p.wallet.feerelayer

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.feerelayer.api.FeeRelayerApi
import org.p2p.wallet.feerelayer.api.FeeRelayerDevnetApi
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerCalculationInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInstructionsInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerSwapInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerTopUpInteractor
import org.p2p.wallet.feerelayer.repository.FeeRelayerRemoteRepository
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.infrastructure.network.NetworkModule.getRetrofit
import org.p2p.wallet.infrastructure.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.infrastructure.network.feerelayer.FeeRelayerInterceptor

object FeeRelayerModule : InjectionModule {

    private const val FEE_RELAYER_QUALIFIER = "https://fee-relayer.solana.p2p.org"
    override fun create() = module {
        single(named(FEE_RELAYER_QUALIFIER)) {
            val environmentManager = get<NetworkServicesUrlProvider>()
            val url = environmentManager.loadFeeRelayerEnvironment().baseUrl
            getRetrofit(
                baseUrl = url,
                tag = "FeeRelayer",
                interceptor = FeeRelayerInterceptor(get())
            )
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
                dispatchers = get(),
                tokenKeyProvider = get()
            )
        }

        factoryOf(::FeeRelayerInteractor)
        factoryOf(::FeeRelayerTopUpInteractor)
        factoryOf(::FeeRelayerInstructionsInteractor)
        factoryOf(::FeeRelayerCalculationInteractor)
        singleOf(::FeeRelayerSwapInteractor)
    }
}
