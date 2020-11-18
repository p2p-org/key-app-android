package com.p2p.wowlet

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.p2p.wowlet.utils.Transfer
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.entities.Result
import com.wowlet.entities.local.BalanceInfo
import com.wowlet.entities.local.TweetNaclFast
import com.wowlet.entities.responce.CallRequest
import kotlinx.android.synthetic.main.activity_maintest.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Utils.readInt64
import org.bitcoinj.crypto.MnemonicCode
import org.koin.android.ext.android.inject
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.rpc.Cluster
import org.p2p.solanaj.rpc.RpcClient
import org.p2p.solanaj.rpc.types.AccountInfo
import java.util.*


class MainActivityTest : AppCompatActivity() {
    private val repository: WowletApiCallRepository by inject()
    private val words = mutableListOf<String>(
        "ordinary",
        "cover",
        "language",
        "pole",
        "achieve",
        "pause",
        "focus",
        "core",
        "sing",
        "lady",
        "zoo",
        "fix"
    )
    private val passphrase = ""
    private val TAG = MainActivityTest::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maintest)
        //get seed by word
        val convertToSeed = MnemonicCode.toSeed(words, passphrase)
        val seedRange: ByteArray = Arrays.copyOfRange(convertToSeed, 0, 32)
        val seed = TweetNaclFast.Signature.keyPair_fromSeed(seedRange)
        // get public and secret keys
        val publicKey: String = Base58.encode(seed.publicKey)
        val secretKey: String = Base58.encode(seed.secretKey)

        Log.i(TAG, "onCreate: publicKey " + publicKey)
        Log.i(TAG, "onCreate: secretKey " + secretKey)

        val compiled = byteArrayOf(2, 2, 0, 1, 12, 2, 0, 0, 0, -72, 11, 0, 0, 0, 0, 0, 0)
        val t = Transfer()

        submitBalance.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                repository.getBalance(CallRequest("getBalance", listOf(publicKey))).apply {
                    when (this) {
                        is Result.Success -> withContext(Dispatchers.Main) {
                            getBalance.text = data?.result?.value.toString()
                            Log.i(TAG, "response getBalance: ${data?.result?.value}")
                        }
                        is Result.Error -> withContext(Dispatchers.Main) {
                            getBalance.text = errors.errorMessage
                            Log.i(TAG, "error getBalance ${errors.errorMessage}")
                        }
                    }
                }
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
                    val accountInfo: AccountInfo = client.api.getAccountInfo(PublicKey(mint))
                    var decimals = 0
                    if (accountInfo.getValue().getData() != null) {
                        val dataStr: String = accountInfo.getValue().getData().get(0)
                        val accountInfoData = Base64.getDecoder().decode(dataStr)
                        decimals = accountInfoData[44].toInt()
                    }
                    balances.add(BalanceInfo(account.pubkey, amount, mint, owner, decimals))
                }

                System.out.println("mint "+Arrays.toString(balances.toTypedArray()))
            }

        }
    }
}