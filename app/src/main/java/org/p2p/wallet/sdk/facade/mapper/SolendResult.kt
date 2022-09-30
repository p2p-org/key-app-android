package org.p2p.wallet.sdk.facade.mapper

import com.google.gson.annotations.SerializedName
import org.p2p.wallet.sdk.facade.model.SolendMethodResultError
import org.p2p.wallet.sdk.facade.model.SolendMethodResultSuccess

class SolendResult<SuccessType : SolendMethodResultSuccess>(
    @SerializedName("success")
    val success: SuccessType? = null,
    @SerializedName("error")
    val error: SolendMethodResultError? = null
) {
    fun onResultError(block: (error: SolendMethodResultError) -> Unit): SolendResult<SuccessType> = apply {
        error?.let(block)
    }

    fun onResultSuccess(block: (result: SuccessType) -> Unit): SolendResult<SuccessType> = apply {
        success?.let(block)
    }

    fun getOrThrow(): SuccessType = success ?: throw error ?: IllegalArgumentException("No value to getOrThrow")
}
