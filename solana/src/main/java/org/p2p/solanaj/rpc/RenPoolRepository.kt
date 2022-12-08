package org.p2p.solanaj.rpc

import org.p2p.solanaj.kits.renBridge.renVM.types.ParamsSubmitMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryBlockState
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryConfig
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseSubmitTxMint

interface RenPoolRepository {
    suspend fun getQueryMint(txHash: String): ResponseQueryTxMint
    suspend fun getQueryBlockState(): ResponseQueryBlockState
    suspend fun getQueryConfig(): ResponseQueryConfig
    suspend fun submitTx(
        hash: String,
        mintTx: ParamsSubmitMint.MintTransactionInput,
        selector: String
    ): ResponseSubmitTxMint
}
