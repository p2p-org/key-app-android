package org.p2p.solanaj.rpc

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.kits.renBridge.renVM.types.ParamsSubmitMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryBlockState
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryConfig
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseSubmitTxMint
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.SignatureInformation

interface RpcSolanaRepository {
    suspend fun getQueryMint(params: HashMap<String, String>): ResponseQueryTxMint
    suspend fun getQueryBlockState(): ResponseQueryBlockState
    suspend fun getQueryConfig(): ResponseQueryConfig
    suspend fun submitTx(
        hash: String,
        mintTx: ParamsSubmitMint.MintTransactionInput,
        selector: String
    ): ResponseSubmitTxMint

    suspend fun getAccountInfo(stateKey: PublicKey): AccountInfo
    suspend fun sendTransaction(transaction: Transaction, signer: Account): String
    suspend fun getConfirmedSignaturesForAddress(mintLogAccount: PublicKey, limit: Int): List<SignatureInformation>
}
