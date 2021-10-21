package com.p2p.wallet.swap.serum.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.p2p.wallet.R
import com.p2p.wallet.auth.repository.AuthRemoteRepository
import com.p2p.wallet.infrastructure.db.WalletDatabase
import com.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.infrastructure.security.SecureStorageContract
import com.p2p.wallet.infrastructure.security.SimpleSecureStorage
import com.p2p.wallet.main.api.CompareApi
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.repository.MainDatabaseRepository
import com.p2p.wallet.main.repository.MainLocalRepository
import com.p2p.wallet.restore.interactor.SecretKeyInteractor
import com.p2p.wallet.rpc.api.RpcApi
import com.p2p.wallet.rpc.repository.RpcRemoteRepository
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.swap.interactor.SwapSerializationInteractor
import com.p2p.wallet.swap.interactor.serum.SerumMarketInteractor
import com.p2p.wallet.swap.interactor.serum.SerumOpenOrdersInteractor
import com.p2p.wallet.swap.interactor.serum.SerumSwapInstructionsInteractor
import com.p2p.wallet.swap.interactor.serum.SerumSwapInteractor
import com.p2p.wallet.swap.interactor.serum.SerumSwapMarketInteractor
import com.p2p.wallet.user.api.SolanaApi
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

    private lateinit var userTokens: List<Token.Active>

    private val keys =
        listOf(
            "oval", "you", "token", "plug", "copper", "visa",
            "employ", "link", "sell", "asset", "kick", "sausage"
        )

    private lateinit var context: Context

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var secureStorage: SecureStorageContract
    private lateinit var tokenKeyProvider: TokenKeyProvider

    private lateinit var secretKeyInteractor: SecretKeyInteractor

    private lateinit var database: WalletDatabase

    /* doesn't matter, which environment is selected, both apis' target is mainnet */
    private lateinit var rpcRepository: RpcRepository

    private lateinit var mainLocalRepository: MainLocalRepository

    private lateinit var instructionsInteractor: SerumSwapInstructionsInteractor
    private lateinit var openOrdersInteractor: SerumOpenOrdersInteractor
    private lateinit var marketInteractor: SerumMarketInteractor

    private lateinit var userRepository: UserRepository
    private lateinit var userLocalRepository: UserLocalRepository
    private lateinit var userInteractor: UserInteractor

    private lateinit var swapMarketInteractor: SerumSwapMarketInteractor

    private lateinit var serializationInteractor: SwapSerializationInteractor

    private lateinit var interactor: SerumSwapInteractor

    fun initialize() {
        context = ApplicationProvider.getApplicationContext()

        sharedPreferences = context.getSharedPreferences(
            "${context.packageName}.prefs", Context.MODE_PRIVATE
        )

        val environmentManager: EnvironmentManager = mockk(relaxed = true)

        val serumApi = RetrofitBuilder.getRetrofit().create(RpcApi::class.java)
        rpcRepository = RpcRemoteRepository(
            serumApi = serumApi,
            mainnetApi = serumApi,
            rpcpoolRpcApi = serumApi,
            testnetApi = serumApi,
            environmentManager = environmentManager,
            onlyMainnet = true
        )

        secureStorage = SimpleSecureStorage(sharedPreferences)

        tokenKeyProvider = TokenKeyProvider(secureStorage)

        userLocalRepository = UserInMemoryRepository()
        userRepository = UserRepositoryImpl(
            compareApi = RetrofitBuilder
                .getRetrofit(context.getString(R.string.compareBaseUrl))
                .create(CompareApi::class.java),
            tokenProvider = tokenKeyProvider,
            userLocalRepository = userLocalRepository,
            rpcRepository = rpcRepository,
            environmentManager = environmentManager,
            solanaApi = RetrofitBuilder
                .getRetrofit(context.getString(R.string.solanaTokensBaseUrl))
                .create(SolanaApi::class.java)
        )

        database = Room.inMemoryDatabaseBuilder(context, WalletDatabase::class.java).build()
        mainLocalRepository = MainDatabaseRepository(database.tokenDao())
        userInteractor = UserInteractor(
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

        instructionsInteractor = SerumSwapInstructionsInteractor(rpcRepository)
        openOrdersInteractor = SerumOpenOrdersInteractor(rpcRepository)
        marketInteractor = SerumMarketInteractor(rpcRepository)

        swapMarketInteractor = SerumSwapMarketInteractor(userInteractor)
        serializationInteractor = SwapSerializationInteractor(rpcRepository, tokenKeyProvider)

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

    fun getSwapMarketInteractor(): SerumSwapMarketInteractor = swapMarketInteractor

    fun getTokens() = userTokens

    fun closeDb() {
        database.close()
    }
}