package com.p2p.wallet.swap.orca.utils

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
import com.p2p.wallet.swap.api.InternalWebApi
import com.p2p.wallet.swap.interactor.orca.OrcaAddressInteractor
import com.p2p.wallet.swap.interactor.orca.OrcaInstructionsInteractor
import com.p2p.wallet.swap.interactor.orca.OrcaPoolInteractor
import com.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor2
import com.p2p.wallet.swap.repository.OrcaSwapInternalRemoteRepository
import com.p2p.wallet.swap.repository.OrcaSwapInternalRepository
import com.p2p.wallet.swap.repository.OrcaSwapRemoteRepository
import com.p2p.wallet.swap.repository.OrcaSwapRepository
import com.p2p.wallet.swap.serum.utils.RetrofitBuilder
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

class OrcaDataInitializer {

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

    private lateinit var userRepository: UserRepository
    private lateinit var userLocalRepository: UserLocalRepository
    private lateinit var userInteractor: UserInteractor

    private lateinit var orcaSwapRepository: OrcaSwapRepository
    private lateinit var orcaSwapInternalRepository: OrcaSwapInternalRepository
    private lateinit var orcaPoolInteractor: OrcaPoolInteractor
    private lateinit var instructionsInteractor: OrcaInstructionsInteractor
    private lateinit var addressInteractor: OrcaAddressInteractor

    private lateinit var interactor: OrcaSwapInteractor2

    fun initialize(shouldMockRepo: Boolean = false) {
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

        orcaSwapRepository = if (shouldMockRepo) {
            OrcaSwapMockRepository()
        } else {
            OrcaSwapRemoteRepository(
                rpcRepository = rpcRepository,
                environmentManager = environmentManager
            )
        }

        orcaSwapInternalRepository = OrcaSwapInternalRemoteRepository(
            webApi = RetrofitBuilder
                .getRetrofit(context.getString(R.string.p2pWebBaseUrl))
                .create(InternalWebApi::class.java),
            environmentManager = environmentManager
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

        orcaPoolInteractor = OrcaPoolInteractor(
            orcaSwapRepository = orcaSwapRepository
        )

        addressInteractor = OrcaAddressInteractor(
            rpcRepository = rpcRepository,
            userLocalRepository = userLocalRepository
        )

        instructionsInteractor = OrcaInstructionsInteractor(
            rpcRepository = rpcRepository,
            orcaAddressInteractor = addressInteractor,
            userInteractor = userInteractor
        )

        interactor = OrcaSwapInteractor2(
            swapRepository = orcaSwapRepository,
            rpcRepository = rpcRepository,
            internalRepository = orcaSwapInternalRepository,
            poolInteractor = orcaPoolInteractor,
            userInteractor = userInteractor,
            orcaInstructionsInteractor = instructionsInteractor,
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

            interactor.load()
        }
    }

    fun getSwapInteractor(): OrcaSwapInteractor2 = interactor

    fun getTokenKeyProvider(): TokenKeyProvider = tokenKeyProvider

    fun getTokens() = userTokens

    fun closeDb() {
        database.close()
    }
}