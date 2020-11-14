package com.wowlet.data.repository


import com.wowlet.data.dataservice.RetrofitService
import com.wowlet.data.datastore.WowletApiCallRepository
import com.wowlet.data.util.analyzeResponse
import com.wowlet.data.util.makeApiCall
import com.wowlet.data.util.mnemoticgenerator.English
import com.wowlet.data.util.mnemoticgenerator.MnemonicGenerator
import com.wowlet.data.util.mnemoticgenerator.Words
import com.wowlet.entities.Result
import com.wowlet.entities.local.SendTransactionModel
import com.wowlet.entities.local.TweetNaclFast
import com.wowlet.entities.local.UserSecretData
import com.wowlet.entities.responce.CallRequest
import com.wowlet.entities.responce.ResponseData
import com.wowlet.entities.responce.ResponseDataAirDrop
import org.bitcoinj.core.Base58
import org.bitcoinj.crypto.MnemonicCode
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.rpc.RpcClient
import retrofit2.Response
import java.security.SecureRandom
import java.util.*

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

    private fun generatePhrase(): List<String> {
        val sb = StringBuilder()
        val entropy = ByteArray(Words.TWELVE.byteLength())
        SecureRandom().nextBytes(entropy)
        MnemonicGenerator(English.INSTANCE)
            .createMnemonic(entropy, sb::append)
        return sb.toString().split(" ")
    }

    private fun getAirdropData(response: Response<ResponseDataAirDrop>): Result<ResponseDataAirDrop> =
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