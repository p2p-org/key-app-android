package org.p2p.wallet.striga.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.create
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.wallet.R
import org.p2p.wallet.infrastructure.network.interceptor.StrigaProxyApiInterceptor
import org.p2p.wallet.smsinput.SmsInputContract
import org.p2p.wallet.smsinput.SmsInputFactory
import org.p2p.wallet.smsinput.SmsInputTimer
import org.p2p.wallet.striga.common.StrigaIpAddressProvider
import org.p2p.wallet.striga.iban.StrigaUserIbanDetailsContract
import org.p2p.wallet.striga.iban.StrigaUserIbanDetailsPresenter
import org.p2p.wallet.striga.iban.StrigaUserIbanUiMapper
import org.p2p.wallet.striga.sms.StrigaSmsApiCaller
import org.p2p.wallet.striga.sms.StrigaSmsInputInteractor
import org.p2p.wallet.striga.sms.onramp.StrigaOnRampSmsApiCaller
import org.p2p.wallet.striga.sms.onramp.StrigaOnRampSmsInputPresenter
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.interactor.StrigaClaimInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
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
    val SMS_QUALIFIER = SmsInputFactory.Type.StrigaOnRamp.name

    override fun create(): Module = module {
        initDataLayer()
        initSms()
        initIban()

        factoryOf(::StrigaClaimInteractor)
        factoryOf(::StrigaWalletInteractor)

        factoryOf(::StrigaIpAddressProvider)
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

    private fun Module.initSms() {
        singleOf(::SmsInputTimer) {
            named(SMS_QUALIFIER)
        }

        factory(named(SMS_QUALIFIER)) { (challengeId: StrigaWithdrawalChallengeId) ->
            StrigaOnRampSmsApiCaller(
                challengeId = challengeId,
                strigaWithdrawalsRepository = get()
            )
        } bind StrigaSmsApiCaller::class

        factory(named(SMS_QUALIFIER)) {
            StrigaSmsInputInteractor(
                strigaSignupDataRepository = get(),
                phoneCodeRepository = get(),
                inAppFeatureFlags = get(),
                smsInputTimer = get(named(SMS_QUALIFIER)),
                strigaStorage = get(),
                smsApiCaller = get(
                    qualifier = named(SMS_QUALIFIER),
                    parameters = { it }
                ),
            )
        }

        factory(named(SMS_QUALIFIER)) {
            StrigaOnRampSmsInputPresenter(
                interactor = get(
                    named(SMS_QUALIFIER),
                    parameters = { it }
                )
            )
        } bind SmsInputContract.Presenter::class
    }

    private fun Module.initIban() {
        factoryOf(::StrigaUserIbanDetailsPresenter) bind StrigaUserIbanDetailsContract.Presenter::class
        factoryOf(::StrigaUserIbanUiMapper)
    }
}
