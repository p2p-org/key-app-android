package org.p2p.wallet.striga.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.create
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.wallet.R
import org.p2p.wallet.infrastructure.network.interceptor.StrigaProxyApiInterceptor
import org.p2p.wallet.striga.iban.StrigaUserIbanDetailsContract
import org.p2p.wallet.striga.iban.StrigaUserIbanDetailsPresenter
import org.p2p.wallet.striga.iban.StrigaUserIbanUiMapper
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.striga.wallet.repository.StrigaWalletRepository
import org.p2p.wallet.striga.wallet.repository.StrigaWhitelistAddressesRepository
import org.p2p.wallet.striga.wallet.repository.StrigaWithdrawalsRepository
import org.p2p.wallet.striga.wallet.repository.impl.StrigaWalletInMemoryRepository
import org.p2p.wallet.striga.wallet.repository.impl.StrigaWalletRemoteRepository
import org.p2p.wallet.striga.wallet.repository.impl.StrigaWhitelistAddressesRemoteRepository
import org.p2p.wallet.striga.wallet.repository.impl.StrigaWithdrawalsRemoteRepository
import org.p2p.wallet.striga.wallet.repository.mapper.StrigaWalletMapper
import org.p2p.wallet.striga.wallet.repository.mapper.StrigaWhitelistAddressesMapper
import org.p2p.wallet.striga.wallet.repository.mapper.StrigaWithdrawalsMapper

object StrigaWalletModule : InjectionModule {

    override fun create(): Module = module {
        initDataLayer()

        factoryOf(::StrigaWalletInteractor)

        factoryOf(::StrigaUserIbanDetailsPresenter) bind StrigaUserIbanDetailsContract.Presenter::class
        factoryOf(::StrigaUserIbanUiMapper)
    }

    private fun Module.initDataLayer() {
        single<StrigaWalletApi> {
            val url = androidContext().getString(R.string.strigaProxyServiceBaseUrl)
            getRetrofit(
                baseUrl = url,
                tag = "StrigaProxyApi",
                interceptor = new(::StrigaProxyApiInterceptor)
            ).create()
        }

        factoryOf(::StrigaWhitelistAddressesMapper)
        factoryOf(::StrigaWhitelistAddressesRemoteRepository) bind StrigaWhitelistAddressesRepository::class

        factoryOf(::StrigaWithdrawalsMapper)
        factoryOf(::StrigaWithdrawalsRemoteRepository) bind StrigaWithdrawalsRepository::class

        factoryOf(::StrigaWalletMapper)
        singleOf(::StrigaWalletInMemoryRepository)
        factoryOf(::StrigaWalletRemoteRepository) bind StrigaWalletRepository::class
    }
}
