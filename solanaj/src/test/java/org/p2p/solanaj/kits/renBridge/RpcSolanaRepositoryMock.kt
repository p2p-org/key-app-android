package org.p2p.solanaj.kits.renBridge

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.kits.renBridge.renVM.types.ParamsSubmitMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryBlockState
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryConfig
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseSubmitTxMint
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.SignatureInformationResponse
import org.p2p.solanaj.rpc.RpcSolanaRepository

class RpcSolanaRepositoryMock: RpcSolanaRepository {

    override suspend fun getQueryMint(txHash: String): ResponseQueryTxMint {
        return ResponseQueryTxMint()
    }

    override suspend fun getQueryBlockState(): ResponseQueryBlockState {
        return ResponseQueryBlockState()
    }

    override suspend fun getQueryConfig(): ResponseQueryConfig {
        return ResponseQueryConfig()
    }

    override suspend fun submitTx(
        hash: String,
        mintTx: ParamsSubmitMint.MintTransactionInput,
        selector: String
    ): ResponseSubmitTxMint {
        return ResponseSubmitTxMint()
    }

    override suspend fun getAccountInfo(stateKey: PublicKey): AccountInfo {
        return AccountInfo()
    }

    override suspend fun sendTransaction(transaction: Transaction, signer: Account): String {
        return "SUCCESS"
    }

    override suspend fun getConfirmedSignaturesForAddress(
        mintLogAccount: PublicKey,
        limit: Int
    ): List<SignatureInformationResponse> {
        return emptyList()
    }
}
