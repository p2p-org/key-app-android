package org.p2p.wallet.auth.gateway

import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create
import org.p2p.wallet.auth.gateway.api.GatewayServiceApi
import org.p2p.wallet.auth.gateway.repository.GatewayServiceRemoteRepository
import org.p2p.wallet.auth.gateway.repository.GatewayServiceRepository
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceCreateWalletMapper
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceErrorMapper
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceGetOnboardingMetadataMapper
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceOnboardingMetadataCipher
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceRestoreWalletMapper
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceUpdateMetadataMapper
import org.p2p.wallet.auth.gateway.repository.mapper.PushServiceSignatureFieldGenerator
import org.p2p.wallet.auth.repository.GatewayServiceErrorHandler
import org.p2p.core.common.di.InjectionModule
import org.p2p.wallet.utils.chacha.ChaCha20Poly1305Decryptor
import org.p2p.wallet.utils.chacha.ChaCha20Poly1305Encryptor
import org.p2p.wallet.utils.chacha.ChaCha20Poly1305SymmetricKeyGenerator

object GatewayServiceModule : InjectionModule {

    const val FACADE_SERVICE_RETROFIT_QUALIFIER = "FACADE_SERVICE_RETROFIT_QUALIFIER"

    override fun create() = module {
        single<GatewayServiceApi> { get<Retrofit>(named(FACADE_SERVICE_RETROFIT_QUALIFIER)).create() }
        factoryOf(::GatewayServiceErrorMapper)
        singleOf(::GatewayServiceCreateWalletMapper)
        singleOf(::GatewayServiceRestoreWalletMapper)
        singleOf(::GatewayServiceGetOnboardingMetadataMapper)
        singleOf(::GatewayServiceUpdateMetadataMapper)
        singleOf(::PushServiceSignatureFieldGenerator)
        factory { ChaCha20Poly1305Encryptor(chaCha20Poly1305 = ChaCha20Poly1305()) }
        factory { ChaCha20Poly1305Decryptor(chaCha20Poly1305 = ChaCha20Poly1305()) }
        factoryOf(::ChaCha20Poly1305SymmetricKeyGenerator)
        factoryOf(::GatewayServiceOnboardingMetadataCipher)
        factoryOf(::GatewayServiceErrorHandler)

        single {
            GatewayServiceRemoteRepository(
                api = get(),
                rpcApi = get(),
                gson = get(),
                urlProvider = get(),
                createWalletMapper = get(),
                restoreWalletMapper = get(),
                getOnboardingMetadataMapper = get(),
                updateMetadataMapper = get(),
                errorMapper = get(),
                dispatchers = get(),
                appScope = get()
            )
        } bind GatewayServiceRepository::class
    }
}
