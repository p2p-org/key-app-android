package org.p2p.solanaj.rpc

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.kits.renBridge.renVM.types.ParamsSubmitMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryBlockState
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseSubmitTxMint
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.SignatureInformation

interface BlockChainRepository {

    suspend fun getAccountInfo(account: String): AccountInfo?

    suspend fun sendTransaction(transaction: Transaction, signers: List<Account>): String

    suspend fun getConfirmedSignaturesForAddress(
        account: PublicKey,
        before: String?,
        limit: Int
    ): List<SignatureInformation>

    suspend fun queryMint(txHash: String): ResponseQueryTxMint

    suspend fun queryBlockState(): ResponseQueryBlockState

    suspend fun submitTx(
        hash: String?,
        mintTx: ParamsSubmitMint.MintTransactionInput?,
        selector: String?
    ): ResponseSubmitTxMint
}