// package org.p2p.wallet.swap.orca.utils
//
// import android.content.Context
// import android.content.SharedPreferences
// import androidx.room.Room
// import androidx.test.core.app.ApplicationProvider
// import io.mockk.mockk
// import kotlinx.coroutines.delay
// import kotlinx.coroutines.runBlocking
// import org.p2p.solanaj.crypto.DerivationPath
// import org.p2p.wallet.R
// import org.p2p.wallet.auth.api.UsernameApi
// import org.p2p.wallet.auth.interactor.UsernameInteractor
// import org.p2p.wallet.auth.repository.AuthRemoteRepository
// import org.p2p.wallet.auth.repository.FileRepository
// import org.p2p.wallet.auth.repository.UsernameRemoteRepository
// import org.p2p.wallet.auth.repository.UsernameRepository
// import org.p2p.wallet.infrastructure.db.WalletDatabase
// import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
// import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
// import org.p2p.wallet.infrastructure.security.SecureStorageContract
// import org.p2p.wallet.infrastructure.security.SimpleSecureStorage
// import org.p2p.wallet.main.api.CompareApi
// import org.p2p.wallet.main.repository.MainDatabaseRepository
// import org.p2p.wallet.main.repository.MainLocalRepository
// import org.p2p.wallet.restore.interactor.SecretKeyInteractor
// import org.p2p.wallet.rpc.api.RpcApi
// import org.p2p.wallet.rpc.repository.RpcRemoteRepository
// import org.p2p.wallet.rpc.repository.RpcRepository
// import org.p2p.wallet.swap.api.InternalWebApi
// import org.p2p.wallet.swap.interactor.SwapInstructionsInteractor
// import org.p2p.wallet.swap.interactor.SwapSerializationInteractor
// import org.p2p.wallet.swap.interactor.orca.OrcaAddressInteractor
// import org.p2p.wallet.swap.interactor.orca.OrcaInstructionsInteractor
// import org.p2p.wallet.swap.interactor.orca.OrcaPoolInteractor
// import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
// import org.p2p.wallet.swap.model.orca.OrcaPools
// import org.p2p.wallet.swap.model.orca.OrcaSwapResult
// import org.p2p.wallet.swap.repository.OrcaSwapInternalRemoteRepository
// import org.p2p.wallet.swap.repository.OrcaSwapInternalRepository
// import org.p2p.wallet.swap.repository.OrcaSwapRemoteRepository
// import org.p2p.wallet.swap.repository.OrcaSwapRepository
// import org.p2p.wallet.swap.serum.utils.RetrofitBuilder
// import org.p2p.wallet.user.api.SolanaApi
// import org.p2p.wallet.user.interactor.UserInteractor
// import org.p2p.wallet.user.repository.UserInMemoryRepository
// import org.p2p.wallet.user.repository.UserLocalRepository
// import org.p2p.wallet.user.repository.UserRepository
// import org.p2p.wallet.user.repository.UserRemoteRepository
//
// class OrcaDataI
// nitializer {
//
//    private lateinit var pools: OrcaPools
//
//    private val keys =
//        listOf(
//            "miracle",
//            "pizza",
//            "supply",
//            "useful",
//            "steak",
//            "border",
//            "same",
//            "again",
//            "youth",
//            "silver",
//            "access",
//            "hundred"
//        )
//
//    private lateinit var context: Context
//
//    private lateinit var sharedPreferences: SharedPreferences
//    private lateinit var secureStorage: SecureStorageContract
//    private lateinit var tokenKeyProvider: TokenKeyProvider
//
//    private lateinit var secretKeyInteractor: SecretKeyInteractor
//
//    private lateinit var database: WalletDatabase
//
//    /* doesn't matter, which environment is selected, both apis' target is mainnet */
//    private lateinit var rpcRepository: RpcRepository
//
//    private lateinit var mainLocalRepository: MainLocalRepository
//
//    private lateinit var userRepository: UserRepository
//    private lateinit var userLocalRepository: UserLocalRepository
//    private lateinit var userInteractor: UserInteractor
//    private lateinit var swapInstructionsInteractor: SwapInstructionsInteractor
//
//    private lateinit var orcaSwapRepository: OrcaSwapRepository
//    private lateinit var orcaSwapInternalRepository: OrcaSwapInternalRepository
//    private lateinit var orcaRouteInteractor: OrcaPoolInteractor
//    private lateinit var instructionsInteractor: OrcaInstructionsInteractor
//    private lateinit var addressInteractor: OrcaAddressInteractor
//    private lateinit var serializationInteractor: SwapSerializationInteractor
//
//    private lateinit var usernameInteractor: UsernameInteractor
//    private lateinit var usernameRepository: UsernameRepository
//    private lateinit var fileLocalRepository: FileRepository
//
//    private lateinit var interactor: OrcaSwapInteractor
//
//    fun initialize(shouldMockRepo: Boolean = true) {
//        context = ApplicationProvider.getApplicationContext()
//
//        sharedPreferences = context.getSharedPreferences(
//            "${context.packageName}.prefs", Context.MODE_PRIVATE
//        )
//
//        val environmentManager: EnvironmentManager = mockk(relaxed = true)
//
//        val serumApi = RetrofitBuilder.getRetrofit().create(RpcApi::class.java)
//        rpcRepository = RpcRemoteRepository(
//            serumApi = serumApi,
//            mainnetApi = serumApi,
//            rpcpoolRpcApi = serumApi,
//            testnetApi = serumApi,
//            environmentManager = environmentManager,
//            onlyMainnet = true
//        )
//
//        orcaSwapRepository = if (shouldMockRepo) {
//            OrcaSwapMockRepository()
//        } else {
//            OrcaSwapRemoteRepository(
//                rpcRepository = rpcRepository,
//                environmentManager = environmentManager
//            )
//        }
//
//        orcaSwapInternalRepository = OrcaSwapInternalRemoteRepository(
//            webApi = RetrofitBuilder
//                .getRetrofit(context.getString(R.string.p2pWebBaseUrl))
//                .create(InternalWebApi::class.java),
//            environmentManager = environmentManager
//        )
//
//        secureStorage = SimpleSecureStorage(sharedPreferences)
//
//        tokenKeyProvider = TokenKeyProvider(secureStorage)
//
//        userLocalRepository = UserInMemoryRepository()
//        userRepository = UserRemoteRepository(
//            compareApi = RetrofitBuilder
//                .getRetrofit(context.getString(R.string.compareBaseUrl))
//                .create(CompareApi::class.java),
//            tokenProvider = tokenKeyProvider,
//            userLocalRepository = userLocalRepository,
//            rpcRepository = rpcRepository,
//            environmentManager = environmentManager,
//            solanaApi = RetrofitBuilder
//                .getRetrofit(context.getString(R.string.solanaTokensBaseUrl))
//                .create(SolanaApi::class.java)
//        )
//
//        database = Room.inMemoryDatabaseBuilder(context, WalletDatabase::class.java).build()
//        mainLocalRepository = MainDatabaseRepository(database.tokenDao())
//        userInteractor = UserInteractor(
//            userRepository = userRepository,
//            userLocalRepository = userLocalRepository,
//            mainLocalRepository = mainLocalRepository,
//            rpcRepository = rpcRepository,
//            tokenKeyProvider = tokenKeyProvider
//        )
//
//        usernameRepository = UsernameRemoteRepository(
//            api = RetrofitBuilder
//                .getRetrofit(context.getString(R.string.feeRelayerBaseUrl))
//                .create(UsernameApi::class.java)
//        )
//
//        fileLocalRepository = FileRepository(context)
//
//        usernameInteractor = UsernameInteractor(
//            usernameRepository = usernameRepository,
//            fileLocalRepository = fileLocalRepository,
//            sharedPreferences = sharedPreferences,
//            tokenKeyProvider = tokenKeyProvider
//        )
//
//        secretKeyInteractor = SecretKeyInteractor(
//            authRepository = AuthRemoteRepository(),
//            userLocalRepository = userLocalRepository,
//            rpcRepository = rpcRepository,
//            tokenProvider = tokenKeyProvider,
//            sharedPreferences = sharedPreferences,
//            usernameInteractor = usernameInteractor
//        )
//
//        addressInteractor = OrcaAddressInteractor(
//            rpcRepository = rpcRepository,
//            userLocalRepository = userLocalRepository
//        )
//
//        swapInstructionsInteractor = SwapInstructionsInteractor(rpcRepository, addressInteractor)
//
//        orcaRouteInteractor = OrcaPoolInteractor(
//            orcaSwapRepository = orcaSwapRepository,
//            instructionsInteractor = swapInstructionsInteractor
//        )
//
//        instructionsInteractor = OrcaInstructionsInteractor(addressInteractor)
//
//        serializationInteractor = SwapSerializationInteractor(rpcRepository, tokenKeyProvider)
//
//        interactor = OrcaSwapInteractor(
//            swapRepository = orcaSwapRepository,
//            rpcRepository = rpcRepository,
//            internalRepository = orcaSwapInternalRepository,
//            poolInteractor = orcaRouteInteractor,
//            userInteractor = userInteractor,
//            orcaInstructionsInteractor = instructionsInteractor,
//            tokenKeyProvider = tokenKeyProvider,
//            serializationInteractor = serializationInteractor
//        )
//
//        runBlocking {
//            userInteractor.loadTokenPrices("USD")
//            userInteractor.loadAllTokensData()
//
//            // make sure tokens data are all downloaded
//            delay(1000L)
//            secretKeyInteractor.createAndSaveAccount(DerivationPath.BIP32DEPRECATED, keys)
//            userInteractor.loadUserTokensAndUpdateData()
//
//            interactor.load()
//
//            pools = orcaSwapInternalRepository.getPools()
//        }
//    }
//
//    fun getSwapInteractor(): OrcaSwapInteractor = interactor
//
//    fun getTokenKeyProvider(): TokenKeyProvider = tokenKeyProvider
//
//    suspend fun fillPoolsBalancesAndSwap(
//        fromWalletPubkey: String,
//        toWalletPubkey: String?,
//        bestPoolsPair: List<RawPool>,
//        amount: Double,
//        slippage: Double,
//        isSimulation: Boolean
//    ): OrcaSwapResult {
//        val bestPair = bestPoolsPair.mapNotNull { rawPool ->
//            var pool = pools[rawPool.name] ?: return@mapNotNull null
//            if (rawPool.reversed) {
//                pool = pool.reversed
//            }
//
//            val balanceA = orcaSwapRepository.loadTokenBalance(pool.tokenAccountA)
//            val balanceB = orcaSwapRepository.loadTokenBalance(pool.tokenAccountB)
//
//            pool.tokenABalance = balanceA
//            pool.tokenBBalance = balanceB
//            pool
//        }
//
//        return interactor.swap(
//            fromWalletPubkey = fromWalletPubkey,
//            toWalletPubkey = toWalletPubkey,
//            bestPoolsPair = bestPair as MutableList,
//            amount = amount,
//            slippage = slippage,
//            isSimulation = isSimulation
//        )
//    }
//
//    fun closeDb() {
//        database.close()
//    }
//
//    data class RawPool(
//        val name: String,
//        val reversed: Boolean
//    )
// }
