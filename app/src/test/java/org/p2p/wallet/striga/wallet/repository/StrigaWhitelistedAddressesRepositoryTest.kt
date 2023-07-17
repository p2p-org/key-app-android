package org.p2p.wallet.striga.wallet.repository

import assertk.all
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.intellij.lang.annotations.Language
import org.junit.Assert
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.models.StrigaBlockchainNetworkInfo
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaWhitelistedAddressItem
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId
import org.p2p.wallet.striga.wallet.repository.impl.StrigaWhitelistAddressesRemoteRepository
import org.p2p.wallet.striga.wallet.repository.mapper.StrigaWhitelistAddressesMapper
import org.p2p.wallet.utils.assertThat
import org.p2p.wallet.utils.fromJson

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaWhitelistedAddressesRepositoryTest {

    private val gson = Gson()
    private val api: StrigaWalletApi = mockk()
    private val userId = "65b1c37c-686a-487b-81f4-8d0ea6dd0e53"

    @Test
    fun `GIVEN whitelisted addresses 200 WHEN getWhitelistedAddresses THEN check response parsed correctly`() =
        runTest {
            @Language("JSON")
            val responseBody = """
            {
                "addresses":[
                    {
                        "id":"5945b161-4534-4ada-bbb5-311e1869fe1b",
                        "address":"0xcab7f66371b19b932b8bcecd8998a392026f3237",
                        "currency":"USDC",
                        "status":"ACTIVATED",
                        "network":{
                            "name":"USD Coin Test (Goerli)",
                            "type":"ERC20",
                            "contractAddress": "0x07865c6E87B9F70255377e024ace6630C1Eaa37F"
                        },
                        "internal":false
                    }
                ],
                "count":1,
                "total":1
            }
            """.trimIndent()

            coEvery { api.getWhitelistedAddresses(any()) } returns responseBody.fromJson(gson)

            val repository = createRepository()
            val result = repository.getWhitelistedAddresses(userId)

            result.assertThat()
                .isInstanceOf(StrigaDataLayerResult.Success::class)
                .transform { it.value as List<StrigaWhitelistedAddressItem> }
                .all {
                    hasSize(1)
                    index(0).all {
                        prop(StrigaWhitelistedAddressItem::id)
                            .isEqualTo(StrigaWhitelistedAddressId("5945b161-4534-4ada-bbb5-311e1869fe1b"))
                        prop(StrigaWhitelistedAddressItem::address)
                            .isEqualTo("0xcab7f66371b19b932b8bcecd8998a392026f3237")
                        prop(StrigaWhitelistedAddressItem::currency)
                            .isEqualTo(StrigaNetworkCurrency.USDC)
                        prop(StrigaWhitelistedAddressItem::status)
                            .isEqualTo(StrigaWhitelistedAddressItem.Status.ACTIVATED)
                        prop(StrigaWhitelistedAddressItem::network).all {
                            prop(StrigaBlockchainNetworkInfo::type).isEqualTo("ERC20")
                            prop(StrigaBlockchainNetworkInfo::contractAddress).isEqualTo("0x07865c6E87B9F70255377e024ace6630C1Eaa37F")
                        }
                    }
                }
        }

    @Test
    fun `GIVEN eth address WHEN add address to striga whitelist THEN check response parsed correctly`() = runTest {
        @Language("JSON")
        val responseBody = """
            {
                "id":"5945b161-4534-4ada-bbb5-311e1869fe1b",
                "status":"PENDING_ACTIVATION",
                "address":"0xcab7f66371b19b932b8bcecd8998a392026f3237",
                "currency":"USDC",
                "network":{
                    "name":"USD Coin Test (Goerli)",
                    "type":"ERC20",
                    "contractAddress":"0x07865c6E87B9F70255377e024ace6630C1Eaa37F"
                }
            }
        """.trimIndent()

        coEvery { api.addWhitelistedAddress(any()) } returns responseBody.fromJson(gson)

        val repo = createRepository()
        val result = repo.whitelistAddress(
            address = "0xcab7f66371b19b932b8bcecd8998a392026f3237",
            currency = StrigaNetworkCurrency.USDC
        )

        Assert.assertTrue(result is StrigaDataLayerResult.Success)
        result as StrigaDataLayerResult.Success<StrigaWhitelistedAddressItem>

        Assert.assertEquals("5945b161-4534-4ada-bbb5-311e1869fe1b", result.value.id.value)
        Assert.assertEquals(StrigaWhitelistedAddressItem.Status.PENDING_ACTIVATION, result.value.status)
        Assert.assertEquals("0xcab7f66371b19b932b8bcecd8998a392026f3237", result.value.address)
        Assert.assertEquals(StrigaNetworkCurrency.USDC, result.value.currency)
        Assert.assertEquals("USD Coin Test (Goerli)", result.value.network.name)
        Assert.assertEquals("ERC20", result.value.network.type)
        Assert.assertEquals("0x07865c6E87B9F70255377e024ace6630C1Eaa37F", result.value.network.contractAddress)
    }

    private fun createRepository(): StrigaWhitelistAddressesRepository {
        return StrigaWhitelistAddressesRemoteRepository(
            api = api,
            mapper = StrigaWhitelistAddressesMapper(),
            strigaUserIdProvider = mockk {
                every { getUserId() } returns userId
                every { getUserIdOrThrow() } returns userId
            }
        )
    }
}
