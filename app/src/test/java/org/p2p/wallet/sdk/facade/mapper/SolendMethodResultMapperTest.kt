package org.p2p.wallet.sdk.facade.mapper

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.p2p.wallet.sdk.facade.model.KeyAppSdkMethodResultException
import org.p2p.wallet.sdk.facade.model.solend.SolendMarketInformationResponse

class SolendMethodResultMapperTest {

    @Test
    fun `given success json when fromSdk called then return success model`() {
        // when
        val mapper = SdkMethodResultMapper(Gson())
        val result = mapper.fromSdk<SolendMarketInformationResponse>(givenSuccessJson)
        // then
        assertThat(result)
            .hasNoNullFieldsOrProperties()
    }

    @Test
    fun `given error json when fromSdk called then throw error model`() {
        // when
        val mapper = SdkMethodResultMapper(Gson())
        // then
        assertThatThrownBy { mapper.fromSdk<SolendMarketInformationResponse>(givenErrorJson) }
            .isInstanceOf(KeyAppSdkMethodResultException::class.java)
            .message()
            .isNotBlank
            .isNotNull
    }

    @Test
    fun `given empty json when fromSdk called then throw IllegalStateException`() {
        // when
        val mapper = SdkMethodResultMapper(Gson())
        // then
        assertThatThrownBy { mapper.fromSdk<SolendMarketInformationResponse>("{}") }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `given invalid json when fromSdk called then throw JsonSyntaxException from gson`() {
        // when
        val mapper = SdkMethodResultMapper(Gson())
        // then
        assertThatThrownBy { mapper.fromSdk<SolendMarketInformationResponse>("231--") }
            .isInstanceOf(JsonSyntaxException::class.java)
    }

    @Language("JSON")
    private val givenErrorJson = """
        { "error": "example error description" } 
    """.trimIndent()

    @Language("JSON")
    private val givenSuccessJson = """
        {
          "success": {
            "market_info": [
              [
                "SOL",
                {
                  "current_supply": "2979635535450699.8285629606626",
                  "deposit_limit": "5000000000000000",
                  "supply_interest": "3.0882726011603356"
                }
              ],
              [
                "USDT",
                {
                  "current_supply": "28982000579693.328266801655979",
                  "deposit_limit": "40000000000000",
                  "supply_interest": "3.985453696506669"
                }
              ],
              [
                "USDC",
                {
                  "current_supply": "108619629909259.90399029585979",
                  "deposit_limit": "150000000000000",
                  "supply_interest": "3.026519657149729"
                }
              ],
              [
                "BTC",
                {
                  "current_supply": "1051508679.5975644239845481567",
                  "deposit_limit": "2500000000",
                  "supply_interest": "0.027548429255541684"
                }
              ],
              [
                "ETH",
                {
                  "current_supply": "261478528712.51281951003393322",
                  "deposit_limit": "2500000000000",
                  "supply_interest": "0.7355250250138168"
                }
              ]
            ]
          }
        }
    """.trimIndent()
}
