package com.p2p.wallet.auth

import android.content.Context
import androidx.biometric.BiometricManager
import com.google.gson.Gson
import com.p2p.wallet.BuildConfig
import com.p2p.wallet.R
import com.p2p.wallet.auth.api.UsernameApi
import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.auth.interactor.ReservingUsernameInteractor
import com.p2p.wallet.auth.repository.AuthRemoteRepository
import com.p2p.wallet.auth.repository.AuthRepository
import com.p2p.wallet.auth.repository.UsernameRemoteRepository
import com.p2p.wallet.auth.repository.UsernameRepository
import com.p2p.wallet.auth.ui.biometric.BiometricContract
import com.p2p.wallet.auth.ui.biometric.BiometricPresenter
import com.p2p.wallet.auth.ui.pin.create.CreatePinContract
import com.p2p.wallet.auth.ui.pin.create.CreatePinPresenter
import com.p2p.wallet.auth.ui.pin.signin.SignInPinContract
import com.p2p.wallet.auth.ui.pin.signin.SignInPinPresenter
import com.p2p.wallet.auth.ui.security.SecurityKeyContract
import com.p2p.wallet.auth.ui.security.SecurityKeyPresenter
import com.p2p.wallet.auth.ui.username.ReservingUsernameContract
import com.p2p.wallet.auth.ui.username.ReservingUsernamePresenter
import com.p2p.wallet.user.UserModule.createLoggingInterceptor
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AuthModule {

    private const val RESERVING_USERNAME_QUALIFIER = "https://fee-relayer.solana.p2p.org"

    fun create() = module {
        single { BiometricManager.from(get()) }

        factory { AuthInteractor(get(), get(), get(), get(), get()) }
        factory { AuthRemoteRepository() } bind AuthRepository::class
        factory { SecurityKeyPresenter(get()) } bind SecurityKeyContract.Presenter::class
        factory { BiometricPresenter(get()) } bind BiometricContract.Presenter::class
        factory { CreatePinPresenter(get()) } bind CreatePinContract.Presenter::class
        factory { SignInPinPresenter(get()) } bind SignInPinContract.Presenter::class

        // reserving username
        factory { ReservingUsernameInteractor(get()) }
        factory { ReservingUsernamePresenter(get()) } bind ReservingUsernameContract.Presenter::class
        factory { get<Retrofit>(named(RESERVING_USERNAME_QUALIFIER)).create(UsernameApi::class.java) }
        single {
            val client = OkHttpClient.Builder()
                .apply { if (BuildConfig.DEBUG) addInterceptor(createLoggingInterceptor("solanaUsername")) }
                .build()
            val api = Retrofit.Builder()
                .baseUrl(get<Context>().getString(R.string.solanaUsername))
                .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
                .client(client)
                .build()
                .create(UsernameApi::class.java)
            UsernameRemoteRepository(api)
        } bind UsernameRepository::class
    }
}