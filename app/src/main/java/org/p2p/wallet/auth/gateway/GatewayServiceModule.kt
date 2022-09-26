package org.p2p.wallet.auth.gateway

import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.auth.gateway.api.GatewayServiceApi
import org.p2p.wallet.auth.gateway.repository.GatewayServiceRemoteRepository
import org.p2p.wallet.auth.gateway.repository.GatewayServiceRepository
import org.p2p.wallet.auth.gateway.repository.mapper.ChaCha20Poly1305Decryptor
import org.p2p.wallet.auth.gateway.repository.mapper.ChaCha20Poly1305Encryptor
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceCreateWalletMapper
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceErrorMapper
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceOnboardingMetadataCipher
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceRestoreWalletMapper
import org.p2p.wallet.auth.gateway.repository.mapper.GatewayServiceSignatureFieldGenerator
import org.p2p.wallet.common.di.InjectionModule
import retrofit2.Retrofit
import retrofit2.create

object GatewayServiceModule : InjectionModule {

    const val FACADE_SERVICE_RETROFIT_QUALIFIER = "FACADE_SERVICE_RETROFIT_QUALIFIER"

    override fun create() = module {
        single<GatewayServiceApi> { get<Retrofit>(named(FACADE_SERVICE_RETROFIT_QUALIFIER)).create() }
        factoryOf(::GatewayServiceErrorMapper)
        singleOf(::GatewayServiceCreateWalletMapper)
        singleOf(::GatewayServiceRestoreWalletMapper)
        singleOf(::GatewayServiceSignatureFieldGenerator)
        factory { ChaCha20Poly1305Encryptor(chaCha20Poly1305 = ChaCha20Poly1305()) }
        factory { ChaCha20Poly1305Decryptor(chaCha20Poly1305 = ChaCha20Poly1305()) }
        factoryOf(::GatewayServiceOnboardingMetadataCipher)

        singleOf(::GatewayServiceRemoteRepository) bind GatewayServiceRepository::class
    }
}
