package org.p2p.wallet.pnl.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.network.gson.GsonProvider
import org.p2p.wallet.pnl.models.PnlData
import org.p2p.wallet.pnl.models.PnlTokenData

class PnlResponseDeserializer : JsonDeserializer<PnlData> {
    private val gson = GsonProvider().provide()
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): PnlData {
        // "total": {
        //      "usd_amount":"+1.23",
        //      "percent":"-3.45"
        //    },
        //    "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v": {
        //      "usd_amount":"+1.23",
        //      "percent":"-3.45"
        //    },
        //    "CKfatsPMUf8SkiURsDXs7eK6GWb4Jsd6UDbs7twMCWxo": {
        //      "usd_amount":"+1.23",
        //      "percent":"-3.45"
        //    }

        val jsonNotNull = json ?: error("PNL response is null")

        val total = gson.fromJson(
            jsonNotNull.asJsonObject.get("total").asJsonObject,
            PnlTokenData::class.java
        )

        val tokens = jsonNotNull.asJsonObject.entrySet()
            .filter { it.key != "total" }
            .associate { it.key.toBase58Instance() to gson.fromJson(it.value, PnlTokenData::class.java) }

        return PnlData(total, tokens)
    }
}
