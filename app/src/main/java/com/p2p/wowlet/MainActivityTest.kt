package com.p2p.wowlet

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.squareup.moshi.*
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.entities.local.BalanceInfo
import kotlinx.android.synthetic.main.activity_maintest.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Utils.readInt64
import org.koin.android.ext.android.inject
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.programs.TokenSwapProgram
import org.p2p.solanaj.rpc.Cluster
import org.p2p.solanaj.rpc.RpcClient
import org.p2p.solanaj.rpc.types.AccountInfo
import java.math.BigInteger
import java.util.*


class MainActivityTest : AppCompatActivity() {
    private val repository: WowletApiCallRepository by inject()
    private val words = mutableListOf<String>(
        "miracle",
        "pizza",
        "supply",
        "useful",
        "steak",
        "border",
        "same",
        "again",
        "youth",
        "silver",
        "access",
        "hundred"
    )


    private val passphrase = ""
    private val TAG = MainActivityTest::class.java.simpleName
    val SPL_TOKEN_PROGRAM_ID = PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA")
    val SYSVAR_RENT_ADDRESS = PublicKey("SysvarRent111111111111111111111111111111111")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maintest)
        val account = Account.fromMnemonic(words, "")
        val secretKey: String = Base58.encode(account.secretKey)
        Log.i(TAG, "onCreate: publicKey " + account.publicKey.toBase58())
        Log.i(TAG, "onCreate: secretKey " + secretKey)

        submitBalance.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val client = RpcClient(Cluster.MAINNET)
                val publicKey = PublicKey("9qvG1zUp8xF1Bi4m6UdRNby1BAAuaDrUxSpv4CmRRMjL")
                val pools = Pool.getPools(client, publicKey)

                pools.forEach {
                    Log.i(TAG, "mintA: ${it.swapData.mintA}")
                    Log.i(TAG, "mintB: ${it.swapData.mintB}")
                }

                val moshi = Moshi.Builder().add(BigIntegerAdapter).build()
                val listMyData = Types.newParameterizedType(
                    MutableList::class.java,
                    Pool.PoolInfo::class.java
                )
                val adapter: JsonAdapter<List<Pool.PoolInfo>> = moshi.adapter<List<Pool.PoolInfo>>(listMyData)

//                        println("pool: ${adapter.toJson(pools)}")

//                val programAccounts = client.api.getProgramSwapAccounts(publicKey.toBase58())
//                if (programAccounts.isNotEmpty()) {
//                    for (programAccount in programAccounts) {
//                        val dataStr = programAccount.account.data[0]
//                        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            Base64.getDecoder().decode(dataStr)
//                        } else {
//                            android.util.Base64.decode(dataStr, android.util.Base64.DEFAULT)
//                        }
//
//                        val tokenSwapData = TokenSwapProgram.TokenSwapData.decode(data)
//
//                        Log.i(TAG, "isInitialized: ${tokenSwapData.isInitialized}")
//                        Log.i(TAG, "nonce: ${tokenSwapData.nonce}")
//                        Log.i(TAG, "tokenProgramId: ${tokenSwapData.tokenProgramId}")
//                        Log.i(TAG, "tokenAccountA: ${tokenSwapData.tokenAccountA}")
//                        Log.i(TAG, "tokenAccountB: ${tokenSwapData.tokenAccountB}")
//                        Log.i(TAG, "tokenPool: ${tokenSwapData.tokenPool}")
//                        Log.i(TAG, "mintA: ${tokenSwapData.mintA}")
//                        Log.i(TAG, "mintB: ${tokenSwapData.mintB}")
//                        Log.i(TAG, "feeAccount: ${tokenSwapData.feeAccount}")
//                        Log.i(TAG, "curveType: ${tokenSwapData.curveType}")
//                        Log.i(TAG, "tradeFeeNumerator: ${tokenSwapData.tradeFeeNumerator}")
//                        Log.i(TAG, "tradeFeeDenominator: ${tokenSwapData.tradeFeeDenominator}")
//                        Log.i(TAG,"ownerTradeFeeNumerator: ${tokenSwapData.ownerTradeFeeNumerator}")
//                        Log.i(TAG,"ownerTradeFeeDenominator: ${tokenSwapData.ownerTradeFeeDenominator}")
//                        Log.i(TAG,"ownerWithdrawFeeNumerator: ${tokenSwapData.ownerWithdrawFeeNumerator}")
//                        Log.i(TAG,"ownerWithdrawFeeDenominator: ${tokenSwapData.ownerWithdrawFeeDenominator}")
//                        Log.i(TAG, "hostFeeNumerator: ${tokenSwapData.hostFeeNumerator}")
//                        Log.i(TAG, "hostFeeDenominator: ${tokenSwapData.hostFeeDenominator}")
//
//
//                        val pool = Pool.getPoolInfo(client, programAccount)
//
//                        val moshi = Moshi.Builder().add(BigIntegerAdapter).build()
//                        val adapter = moshi.adapter(Pool.PoolInfo::class.java)
//
//                        println("pool: ${adapter.toJson(pool)}")
//
//                        break
////
//                    }
////
//////                val payer = Account(
//////                    Base58
//////                        .decode("MdfH9tqq9mkBZ1YmjgmT5tGN9BMEUNgGAXefYNfW2LsNRWUzs7GrhYVvdKwhGL1gXCUT9EjuyaH4VATozcx6jHr")
//////                )
//////                val mintAddress = PublicKey("CWE8jPTUYhdCTZYWPTe1o5DFqfdjzWKc9WKz6rSjQUdG")
//////                val newAccount = Account()
//////
//////                val signature: String =
//////                    createAndInitializeTokenAccount(client, payer, mintAddress, newAccount)
//////
//////                print(signature)
//                }
            }
        }
        submitAirdrop.setOnClickListener {
            /*    CoroutineScope(Dispatchers.IO).launch {
                repository.requestAirdrop(
                    CallRequest(
                        "requestAirdrop",
                        listOf<Any>(publicKey, 50)
                    )
                ).apply {
                    when (this) {
                        is Result.Success -> withContext(Dispatchers.Main) {
                            getAirdrop.text = data?.result
                            Log.i(TAG, "response requestAirdrop  ${data?.result}")
                        }
                        is Result.Error -> withContext(Dispatchers.Main) {
                            getAirdrop.text = errors.errorMessage
                            Log.i(TAG, "error requestAirdrop ${errors.errorMessage}")
                        }
                    }
                }
            }
*/
            /*    CoroutineScope(Dispatchers.IO).launch {
            val client = RpcClient(Cluster.TESTNET)

            val fromPublicKey = PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo")
            val toPublickKey = PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5")
            val lamports = 3000

            val signer = Account(
                Base58
                    .decode("4Z7cXSyeFR8wNGMVXUE1TwtKn5D5Vu7FzEv69dokLv7KrQk7h6pu4LF8ZRR9yQBhc7uSM6RTTZtU1fmaxiNrxXrs")
            )

            val transaction = Transaction()
            transaction.addInstruction(
                SystemProgram.transfer(
                    fromPublicKey,
                    toPublickKey,
                    lamports
                )
            )

            print(client.getApi().sendTransaction(transaction, signer))
        }*/

            /*      CoroutineScope(Dispatchers.IO).launch {
            val client = RpcClient(Cluster.TESTNET)

            val fromPublicKey = PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo")
            val toPublickKey = PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5")
            val lamports = 3000

            val signer = Account(
                Base58
                    .decode("4Z7cXSyeFR8wNGMVXUE1TwtKn5D5Vu7FzEv69dokLv7KrQk7h6pu4LF8ZRR9yQBhc7uSM6RTTZtU1fmaxiNrxXrs")
            )

            val transaction = Transaction()
            transaction.addInstruction(
                SystemProgram.transfer(
                    fromPublicKey,
                    toPublickKey,
                    lamports
                )
            )

            print(client.getApi().getBalance(fromPublicKey))




        }*/
            CoroutineScope(Dispatchers.IO).launch {

                val client = RpcClient(Cluster.MAINNET)

                val accountAddress = "3h1zGmCwsRJnVk5BuRNMLsPaQu1y2aqXqXDWYCgrp5UG"
                try {
                    val programAccounts = client.api
                        .getProgramAccounts(
                            PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"),
                            32,
                            accountAddress
                        )
                    val balances: MutableList<BalanceInfo> = ArrayList()
                    for (account in programAccounts) {
                        val data = Base58.decode(account.account.data)
                        val mintData = ByteArray(32)
                        System.arraycopy(data, 0, mintData, 0, 32)
                        val owherData = ByteArray(32)
                        System.arraycopy(data, 32, owherData, 0, 32)
                        val owner = Base58.encode(owherData)
                        val mint = Base58.encode(mintData)
                        val amount = readInt64(data, 32 + 32)
                        val accountInfo: AccountInfo =
                            client.api.getAccountInfo(PublicKey(mint))
                        var decimals = 0
                        if (accountInfo.getValue().getData() != null) {
                            val dataStr: String = accountInfo.getValue().getData().get(0)
                            val accountInfoData =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    Base64.getDecoder().decode(dataStr)
                                } else {
                                    android.util.Base64.decode(dataStr, android.util.Base64.DEFAULT)
                                }
                            decimals = accountInfoData[44].toInt()
                        }
                        balances.add(BalanceInfo(account.pubkey, amount, mint, owner, decimals))
                    }
                    Log.i("programAccountsmint", Arrays.toString(balances.toTypedArray()))

                } catch (e: Exception) {
                    Log.i(TAG, "onCreate: ${e.message}")
                }

            }

        }

    }
}

object BigIntegerAdapter {
    @FromJson
    fun fromJson(string: String) = BigInteger(string)

    @ToJson
    fun toJson(value: BigInteger) = value.toString()
}