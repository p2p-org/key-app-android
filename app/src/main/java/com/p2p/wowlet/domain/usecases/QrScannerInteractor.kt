package com.p2p.wowlet.domain.usecases

import com.p2p.wowlet.datastore.WowletApiCallRepository
import com.p2p.wowlet.entities.CallException
import com.p2p.wowlet.entities.Constants
import com.p2p.wowlet.entities.Result
import org.json.JSONException
import org.json.JSONObject
import org.p2p.solanaj.core.PublicKey

class QrScannerInteractor(
    private val wowletApiCallRepository: WowletApiCallRepository,
) {

    suspend fun getAccountInfo(publicKey: String): Result<String> {
        return try {
            val accountInfo = wowletApiCallRepository.getQRAccountInfo(PublicKey(publicKey))
            if (accountInfo.value != null) {
                try {
                    (accountInfo.value.data as? ArrayList<*>)?.let {
                        if (it.isNotEmpty() && it[0] == "") {
                            if (accountInfo.value.owner == Constants.OWNER_SOL) {
                                return Result.Success(accountInfo.value.owner)
                            }
                        }
                    }
                    val jsonObject = JSONObject(accountInfo.value.data.toString())
                    val parsed = jsonObject.getString("parsed")
                    if (parsed != "null") {
                        val parsedJson = JSONObject(parsed)
                        val info = parsedJson.getString("info")
                        val infoJson = JSONObject(info)
                        val mint = infoJson.getString("mint")
                        Result.Success(mint)
                    } else {
                        Result.Error(CallException(Constants.ERROR_NULL_DATA, "Parsed is null"))
                    }
                } catch (e: JSONException) {
                    Result.Error(CallException(Constants.ERROR_NULL_DATA, e.message))
                }
            } else {
                // Scanned wallet has 0 balance
                Result.Error(CallException(Constants.ERROR_NULL_DATA, "Could not find the wallet"))
            }
        } catch (e: Exception) {
            Result.Error(CallException(Constants.REQUEST_EXACTION, e.message))
        }
    }
}