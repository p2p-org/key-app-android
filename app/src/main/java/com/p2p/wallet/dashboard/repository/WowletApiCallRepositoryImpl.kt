package com.p2p.wallet.dashboard.repository

import android.os.Build
import com.p2p.wallet.dashboard.api.RetrofitService
import com.p2p.wallet.common.network.Result
import com.p2p.wallet.dashboard.model.local.BalanceInfo
import com.p2p.wallet.dashboard.model.local.SendTransactionModel
import com.p2p.wallet.dashboard.model.local.UserSecretData
import com.p2p.wallet.common.network.ResponceDataBonfida
import com.p2p.wallet.dashboard.model.orderbook.OrderBooks
import com.p2p.wallet.utils.analyzeResponseObject
import com.p2p.wallet.utils.makeApiCall
import com.p2p.wallet.utils.mnemoticgenerator.English
import com.p2p.wallet.utils.mnemoticgenerator.MnemonicGenerator
import com.p2p.wallet.utils.mnemoticgenerator.Words
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Utils
import org.bitcoinj.core.Utils.readInt64
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.rpc.RpcClient
import org.p2p.solanaj.rpc.RpcException
import org.p2p.solanaj.rpc.types.AccountInfo
import org.p2p.solanaj.rpc.types.ConfirmedTransaction
import org.p2p.solanaj.rpc.types.QRAccountInfo
import org.p2p.solanaj.rpc.types.TransferInfo
import retrofit2.Response
import java.security.SecureRandom
import java.util.Arrays
import java.util.Base64

class WowletApiCallRepositoryImpl(
    private val allApiService: RetrofitService,
    private val client: RpcClient
) : WowletApiCallRepository {

    override suspend fun initAccount(phraseList: List<String>): UserSecretData {
        val account = Account.fromMnemonic(phraseList, "")
        val publicKey = Base58.encode(account.publicKey.toByteArray())
        val secretKey = Base58.encode(account.secretKey)

        return UserSecretData(secretKey, publicKey, phraseList)
    }

    override suspend fun sendTransaction(sendTransactionModel: SendTransactionModel): String {
        val fromPublicKey = PublicKey(sendTransactionModel.fromPublicKey)
        val toPublicKey = PublicKey(sendTransactionModel.toPublickKey)
        val signer = Account(Base58.decode(sendTransactionModel.secretKey))

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

    override suspend fun getBalance(accountAddress: String): Long {
        return client.api.getBalance(PublicKey(accountAddress))
    }

    override suspend fun getProgramAccounts(publicKey: String) {
        val programAccounts = client.api
            .getProgramAccounts(
                PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"),
                32,
                publicKey
            )

        programAccounts.map { programAccount ->
            println("owner = ${programAccount.account.owner}")
            println("pub key = ${programAccount.pubkey}")
        }
    }

    override suspend fun getWallets(publicKey: String): MutableList<BalanceInfo> {
        val programAccounts = client.api
            .getProgramAccounts(
                PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"),
                32,
                publicKey
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
            val accountInfo = getAccountInfo(PublicKey(mint))
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

                decimals = accountInfoData[44].toInt()
            }
            balances.add(BalanceInfo(account.pubkey, amount, mint, owner, decimals))
        }

        System.out.println("balances " + Arrays.toString(balances.toTypedArray()))
        return balances
    }

    override suspend fun getAccountInfo(publicKey: PublicKey): AccountInfo {
        return client.api.getAccountInfo(publicKey)
    }

    override suspend fun getQRAccountInfo(publicKey: PublicKey): QRAccountInfo {
        return client.api.getQRAccountInfo(publicKey)
    }

    override suspend fun getMinimumBalance(accountLenght: Long): Long {
        val minimumBalance: Long =
            client.api.getMinimumBalanceForRentExemption(accountLenght)
        return minimumBalance
    }

    override suspend fun getDetailActivityData(publicKey: String): List<TransferInfo> {
        val signatures = client.api
            .getConfirmedSignaturesForAddress2(
                PublicKey(publicKey),
                10
            )
        val transferInfoList = mutableListOf<TransferInfo>()

        for (signature in signatures) {
            println("mint $signature")
            val transferInfo = getConfirmedTransaction(signature.signature, signature.slot.toLong())
            transferInfo?.let {
                transferInfoList.add(transferInfo)
            }
        }
        println("transferInfoList " + transferInfoList.toTypedArray().contentToString())
        return transferInfoList
    }

    override suspend fun getBlockTime(slot: Long): Long {
        return client.api.getBlockTime(slot)
    }

    override suspend fun getFee(): Long = client.api.feeBlockhash

    override suspend fun getConfirmedTransaction(signature: String, slot: Long): TransferInfo? {
        val trx = client.api.getConfirmedTransaction(signature)
        val message: ConfirmedTransaction.Message = trx.transaction.message
        val meta: ConfirmedTransaction.Meta = trx.meta
        val instructions: List<ConfirmedTransaction.Instruction> = message.instructions
        for (instruction in instructions) {
            val number: Long = 2
            if (message.accountKeys[instruction.programIdIndex.toInt()] == "11111111111111111111111111111111") {
                val data = Base58.decode(instruction.data)
                val lamports = readInt64(data, 4)

                val transferInfo = TransferInfo(
                    message.accountKeys[instruction.accounts[0].toInt()],
                    message.accountKeys[instruction.accounts[1].toInt()],
                    lamports
                )
                transferInfo.slot = slot
                transferInfo.signature = signature
                transferInfo.setFee(meta.fee)
                println(transferInfo)
                return transferInfo
            } else {
                val s = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
                if (message.accountKeys[instruction.programIdIndex.toInt()] == s) {
                    val data = Base58.decode(instruction.data)
                    val lamports = readInt64(data, 1)

                    val transferInfo = TransferInfo(
                        message.accountKeys[instruction.accounts[0].toInt()],
                        message.accountKeys[instruction.accounts[1].toInt()],
                        lamports
                    )
                    transferInfo.slot = slot
                    transferInfo.signature = signature
                    transferInfo.setFee(meta.fee)
                    println(transferInfo)
                    return transferInfo
                }
            }
/*
            if (instruction.programIdIndex == number) {
                val data = Base58.decode(instruction.data)
                val lamports = readInt64(data, 4)

                val transferInfo = TransferInfo(
                    message.accountKeys[instruction.accounts[0].toInt()],
                    message.accountKeys[instruction.accounts[1].toInt()],
                    lamports
                )
                transferInfo.slot = slot
                transferInfo.signature = signature
                transferInfo.setFee(meta.fee)
                println(transferInfo)
                return transferInfo
            }else{
                val data = Base58.decode(instruction.data)
                val lamports = readInt64(data, 3)

                val transferInfo = TransferInfo(
                    message.accountKeys[instruction.accounts[0].toInt()],
                    message.accountKeys[instruction.accounts[instruction.programIdIndex.toInt()].toInt()],
                    lamports
                )
                transferInfo.slot = slot
                transferInfo.signature = signature
                transferInfo.setFee(meta.fee)
                println(transferInfo)
                return transferInfo
            }*/
        }
        return null
    }

    @Throws(RpcException::class)
    override suspend fun createAndInitializeTokenAccount(
        payer: Account,
        mintAddress: PublicKey,
        newAccount: Account
    ): String {
        val space = (32 + 32 + 8 + 93).toLong() // mint account data length: 32 + 32 + 8 + 93
        val newAccountPubKey = newAccount.publicKey
        val payerPubKey = payer.publicKey
        val minBalance = client.api.getMinimumBalanceForRentExemption(space)
        val createAccount = SystemProgram.createAccount(
            payerPubKey, newAccountPubKey, minBalance,
            space, SystemProgram.SPL_TOKEN_PROGRAM_ID
        )
        val initializeAccount = SystemProgram.initializeAccountInstruction(
            newAccountPubKey, mintAddress,
            payerPubKey
        )
        val transaction = Transaction()
        transaction.addInstruction(createAccount)
        transaction.addInstruction(initializeAccount)
        return client.api.sendTransaction(transaction, listOf(payer, newAccount))
    }

    override suspend fun generatePhrase(): List<String> = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        val entropy = ByteArray(Words.TWELVE.byteLength())
        SecureRandom().nextBytes(entropy)
        MnemonicGenerator(English.INSTANCE)
            .createMnemonic(entropy, sb::append)
        sb.toString().split(" ")
    }

    override suspend fun getOrderBooks(tokenSymbol: String): Result<OrderBooks> =
        makeApiCall({
            getOrderBooksData(
                allApiService.getOrderBooks(
                    tokenSymbol
                )
            )
        })

    private fun getOrderBooksData(response: Response<ResponceDataBonfida<OrderBooks>>): Result<OrderBooks> =
        analyzeResponseObject(response)
}