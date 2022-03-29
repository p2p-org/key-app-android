package org.p2p.solanaj.kits.transaction.network

import com.google.gson.annotations.SerializedName
import org.p2p.solanaj.kits.transaction.network.meta.MetaResponse
import org.p2p.solanaj.kits.transaction.network.transaction.TransactionInformationResponse
import org.p2p.solanaj.model.types.RpcResultObject

data class ConfirmedTransactionRootResponse(
    @SerializedName("blockTime")
    val blockTime: Long,

    @SerializedName("slot")
    val slot: Int,

    @SerializedName("transaction")
    val transaction: TransactionInformationResponse?,

    @SerializedName("meta")
    val meta: MetaResponse,
) : RpcResultObject()
