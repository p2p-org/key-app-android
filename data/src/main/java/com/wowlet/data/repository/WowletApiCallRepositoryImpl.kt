package com.wowlet.data.repository


import android.os.Build

import com.wowlet.data.dataservice.RetrofitService
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.data.util.analyzeResponse
import com.wowlet.data.util.makeApiCall
import com.wowlet.data.util.mnemoticgenerator.English
import com.wowlet.data.util.mnemoticgenerator.MnemonicGenerator
import com.wowlet.data.util.mnemoticgenerator.Words
import com.wowlet.entities.Result
import com.wowlet.entities.local.*
import com.wowlet.entities.responce.CallRequest
import com.wowlet.entities.responce.ResponseData
import com.wowlet.entities.responce.ResponseDataAirDrop
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Utils
import org.bitcoinj.crypto.MnemonicCode
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.rpc.Cluster
import org.p2p.solanaj.rpc.RpcClient
import org.p2p.solanaj.rpc.types.AccountInfo
import retrofit2.Response
import java.security.SecureRandom
import java.util.*
import kotlin.collections.ArrayList

class WowletApiCallRepositoryImpl(
    private val allApiService: RetrofitService,
    private val client: RpcClient
) : WowletApiCallRepository {

    private var publicKey: String = ""
    private var secretKey: String = ""
    override suspend fun requestAirdrop(requestModel: CallRequest): Result<ResponseDataAirDrop> =
        makeApiCall({
            getAirdropData(
                allApiService.requestAirdrop(
                    requestModel
                )
            )
        })
    private fun getAirdropData(response: Response<ResponseDataAirDrop>): Result<ResponseDataAirDrop> =
        analyzeResponse(response)
    override suspend fun initAccount(): UserSecretData {
        val words = generatePhrase()

        val convertToSeed = MnemonicCode.toSeed(words, "")
        val seedRange: ByteArray = Arrays.copyOfRange(convertToSeed, 0, 32)
        val seed = TweetNaclFast.Signature.keyPair_fromSeed(seedRange)
        // get public and secret keys
        publicKey = Base58.encode(seed.publicKey)
        secretKey = Base58.encode(seed.secretKey)

        return UserSecretData(secretKey, publicKey, seed, words)
    }

    override suspend fun sendTransaction(sendTransactionModel: SendTransactionModel): String {
        val fromPublicKey = PublicKey(publicKey)
        val toPublicKey = PublicKey(sendTransactionModel.toPublickKey)

        val signer = Account(Base58.decode(secretKey))

        val transaction = Transaction()
        transaction.addInstruction(
            SystemProgram.transfer(
                fromPublicKey,
                toPublicKey,
                sendTransactionModel.lamports
            )
        )

        return client.api.sendTransaction(transaction, signer)
    }

    override suspend fun getWallets(publicKey: String): List<BalanceInfo> {


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
            val amount = Utils.readInt64(data, 32 + 32)
            val accountInfo: AccountInfo = client.api.getAccountInfo(PublicKey(mint))
            var decimals = 0
            accountInfo.value.data?.let {
                val dataStr: String = it[0]
                val accountInfoData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Base64.getDecoder().decode(dataStr)
                } else {
                    android.util.Base64.decode(
                        dataStr,
                        android.util.Base64.DEFAULT
                    ) // Unresolved reference: decode
                }
                //  val accountInfoData = Base64.getDecoder().decode(dataStr)
                decimals = accountInfoData[44].toInt()
            }
            balances.add(BalanceInfo(account.pubkey, amount, mint, owner, decimals))

        }
        System.out.println("balances "+Arrays.toString(balances.toTypedArray()))
        return balances
    }


    private fun generatePhrase(): List<String> {
        val sb = StringBuilder()
        val entropy = ByteArray(Words.TWELVE.byteLength())
        SecureRandom().nextBytes(entropy)
        MnemonicGenerator(English.INSTANCE)
            .createMnemonic(entropy, sb::append)
        return sb.toString().split(" ")
    }

    override suspend fun getOrderBooks(tokenSymbol: String): Result<Orderbooks> =
        makeApiCall({
            getOrderBooksData(
                allApiService.getOrderBooks(
                    tokenSymbol
                )
            )
        })
    private fun getOrderBooksData(response: Response<Orderbooks>): Result<Orderbooks> =
        analyzeResponse(response)

    override suspend fun getBalance(requestModel: CallRequest): Result<ResponseData> =
        makeApiCall({
            getBalanceData(
                allApiService.getBalance(
                    requestModel
                )
            )
        })

    private fun getBalanceData(response: Response<ResponseData>): Result<ResponseData> =
        analyzeResponse(response)


}