package com.p2p.wallet.swap.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.p2p.wallet.R
import com.p2p.wallet.auth.repository.AuthRemoteRepository
import com.p2p.wallet.infrastructure.db.WalletDatabase
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.infrastructure.security.SecureStorage
import com.p2p.wallet.main.api.CompareApi
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.repository.MainDatabaseRepository
import com.p2p.wallet.main.repository.MainLocalRepository
import com.p2p.wallet.restore.interactor.SecretKeyInteractor
import com.p2p.wallet.rpc.api.RpcApi
import com.p2p.wallet.rpc.repository.RpcRemoteRepository
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.swap.interactor.MarketInteractor
import com.p2p.wallet.swap.interactor.OpenOrdersInteractor
import com.p2p.wallet.swap.interactor.SerializationInteractor
import com.p2p.wallet.swap.interactor.SerumSwapInteractor
import com.p2p.wallet.swap.interactor.SwapInstructionsInteractor
import com.p2p.wallet.swap.interactor.SwapMarketInteractor
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.user.repository.UserInMemoryRepository
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.user.repository.UserRepository
import com.p2p.wallet.user.repository.UserRepositoryImpl
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.p2p.solanaj.crypto.DerivationPath

class DataInitializer {

    private lateinit var userTokens: List<Token>

    private val keys =
        listOf(
            "oval", "you", "token", "plug", "copper", "visa",
            "employ", "link", "sell", "asset", "kick", "sausage"
        )

    private lateinit var context: Context

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var secureStorage: SecureStorage
    private lateinit var tokenKeyProvider: TokenKeyProvider

    private lateinit var secretKeyInteractor: SecretKeyInteractor

    private lateinit var database: WalletDatabase

    /* doesn't matter, which environment is selected, both apis' target is mainnet */
    private lateinit var rpcRepository: RpcRepository

    private lateinit var mainLocalRepository: MainLocalRepository

    private lateinit var instructionsInteractor: SwapInstructionsInteractor
    private lateinit var openOrdersInteractor: OpenOrdersInteractor
    private lateinit var marketInteractor: MarketInteractor

    private lateinit var userRepository: UserRepository
    private lateinit var userLocalRepository: UserLocalRepository
    private lateinit var userInteractor: UserInteractor

    private lateinit var swapMarketInteractor: SwapMarketInteractor

    private lateinit var serializationInteractor: SerializationInteractor

    private lateinit var interactor: SerumSwapInteractor

    fun initialize() {
        context = ApplicationProvider.getApplicationContext()

        sharedPreferences = context.getSharedPreferences(
            "${context.packageName}.prefs", Context.MODE_PRIVATE
        )

        rpcRepository = RpcRemoteRepository(
            serumApi = RetrofitBuilder.getRetrofit().create(RpcApi::class.java),
            mainnetApi = RetrofitBuilder.getRetrofit().create(RpcApi::class.java),
            testnetApi = RetrofitBuilder.getRetrofit().create(RpcApi::class.java),
            environmentManager = mockk(),
            onlyMainnet = true
        )

        secureStorage = SecureStorage(mockk(), sharedPreferences, isTesting = true)

        tokenKeyProvider = TokenKeyProvider(
            sharedPreferences = sharedPreferences,
            secureStorage = secureStorage,
            context = context
        )

        userLocalRepository = UserInMemoryRepository()
        userRepository = UserRepositoryImpl(
            compareApi = RetrofitBuilder
                .getRetrofit(context.getString(R.string.compareBaseUrl))
                .create(CompareApi::class.java),
            tokenProvider = tokenKeyProvider,
            userLocalRepository = userLocalRepository,
            rpcRepository = rpcRepository
        )

        database = Room.inMemoryDatabaseBuilder(context, WalletDatabase::class.java).build()
        mainLocalRepository = MainDatabaseRepository(database.tokenDao())
        userInteractor = UserInteractor(
            context = context,
            gson = Gson(),
            userRepository = userRepository,
            userLocalRepository = userLocalRepository,
            mainLocalRepository = mainLocalRepository,
            rpcRepository = rpcRepository
        )

        secretKeyInteractor = SecretKeyInteractor(
            authRepository = AuthRemoteRepository(),
            userLocalRepository = userLocalRepository,
            rpcRepository = rpcRepository,
            tokenProvider = tokenKeyProvider,
            sharedPreferences = sharedPreferences
        )

        instructionsInteractor = SwapInstructionsInteractor(rpcRepository)
        openOrdersInteractor = OpenOrdersInteractor(rpcRepository)
        marketInteractor = MarketInteractor(rpcRepository)

        swapMarketInteractor = SwapMarketInteractor(userInteractor)
        serializationInteractor = SerializationInteractor(rpcRepository, tokenKeyProvider)

        interactor = SerumSwapInteractor(
            instructionsInteractor = instructionsInteractor,
            openOrdersInteractor = openOrdersInteractor,
            marketInteractor = marketInteractor,
            swapMarketInteractor = swapMarketInteractor,
            serializationInteractor = serializationInteractor,
            tokenKeyProvider = tokenKeyProvider
        )

        runBlocking {
            userInteractor.loadTokenPrices("USD")
            userInteractor.loadAllTokensData()

            // make sure tokens data are all downloaded
            delay(1000L)
            secretKeyInteractor.createAndSaveAccount(DerivationPath.BIP32DEPRECATED, keys)
            userInteractor.loadUserTokensAndUpdateData()
            userTokens = userInteractor.getUserTokens()
        }
    }

    fun getSwapInteractor(): SerumSwapInteractor = interactor
    fun getSwapMarketInteractor(): SwapMarketInteractor = swapMarketInteractor

    fun getTokens() = userTokens

    fun closeDb() {
        database.close()
    }
}