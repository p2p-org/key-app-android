package org.p2p.wallet.striga.wallet.repository

import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountStatus
import org.p2p.wallet.striga.wallet.models.StrigaInitWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaOnchainTxStatus
import org.p2p.wallet.striga.wallet.models.StrigaOnchainTxType
import org.p2p.wallet.striga.wallet.models.StrigaWhitelistedAddressItem
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId
import org.p2p.wallet.utils.fromJsonReified

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaWalletRepositoryEnrichAccountTest {

    private val gson = Gson()
    private val api: StrigaWalletApi = mockk()
    private val userId = "65b1c37c-686a-487b-81f4-8d0ea6dd0e53"

    @Test
    fun `GIVEN whitelisted addresses response WHEN getWhitelistedAddresses THEN check response parsed correctly`() =
        runTest {
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

            coEvery { api.getWhitelistedAddresses(any()) } returns responseBody.fromJson()

            val repo = createRepository()
            val result = repo.getWhitelistedAddresses(userId)

            assertTrue(result is StrigaDataLayerResult.Success)
            result as StrigaDataLayerResult.Success<List<StrigaWhitelistedAddressItem>>
            assertEquals(1, result.value.size)

            with(result.value.first()) {
                assertEquals("5945b161-4534-4ada-bbb5-311e1869fe1b", id.value)
                assertEquals("0xcab7f66371b19b932b8bcecd8998a392026f3237", address)
                assertEquals(StrigaNetworkCurrency.USDC, currency)
                assertEquals(StrigaWhitelistedAddressItem.Status.ACTIVATED, status)
                assertEquals("USD Coin Test (Goerli)", network.name)
                assertEquals("ERC20", network.type)
                assertEquals("0x07865c6E87B9F70255377e024ace6630C1Eaa37F", network.contractAddress)
            }
        }

    @Test
    fun `GIVEN eth address WHEN add address to striga whitelist THEN check response parsed correctly`() = runTest {
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

        coEvery { api.addWhitelistedAddress(any()) } returns responseBody.fromJson()

        val repo = createRepository()
        val result = repo.whitelistAddress(
            address = "0xcab7f66371b19b932b8bcecd8998a392026f3237",
            currency = StrigaNetworkCurrency.USDC
        )

        assertTrue(result is StrigaDataLayerResult.Success)
        result as StrigaDataLayerResult.Success<StrigaWhitelistedAddressItem>
        assertEquals("5945b161-4534-4ada-bbb5-311e1869fe1b", result.value.id.value)
        assertEquals(StrigaWhitelistedAddressItem.Status.PENDING_ACTIVATION, result.value.status)
        assertEquals("0xcab7f66371b19b932b8bcecd8998a392026f3237", result.value.address)
        assertEquals(StrigaNetworkCurrency.USDC, result.value.currency)
        assertEquals("USD Coin Test (Goerli)", result.value.network.name)
        assertEquals("ERC20", result.value.network.type)
        assertEquals("0x07865c6E87B9F70255377e024ace6630C1Eaa37F", result.value.network.contractAddress)
    }

    @Test
    fun `GIVEN init onchain withdrawal WHEN initiateOnchainWithdrawal THEN check response parsed correctly`() =
        runTest {
            // real response
            val responseBody = """
            {
                "challengeId":"f8e7e49c-3559-4351-a2a4-e70b633744ec",
                "dateExpires":"2023-06-27T13:37:53.356Z",
                "transaction":{
                    "syncedOwnerId":"65b1c37c-686a-487b-81f4-8d0ea6dd0e53",
                    "sourceAccountId":"01c1f4e73d8b2587921c74e98951add0",
                    "parentWalletId":"a927c70c-3678-4d23-b54d-0261dda6bdbb",
                    "currency":"USDC",
                    "amount":"2000",
                    "status":"PENDING_2FA_CONFIRMATION",
                    "txType":"ON_CHAIN_WITHDRAWAL_INITIATED",
                    "blockchainDestinationAddress":"0xcab7f66371b19b932b8bcecd8998a392026f3237",
                    "blockchainNetwork":{
                        "name":"USD Coin Test (Goerli)",
                        "type":"ERC20",
                        "contractAddress":"0x07865c6E87B9F70255377e024ace6630C1Eaa37F"
                     },
                     "transactionCurrency":"USDC"
                },
                "feeEstimate":{
                    "totalFee":"521",
                    "networkFee":"3058854447819900",
                    "ourFee":"521",
                    "theirFee":"0",
                    "feeCurrency":"USDC",
                    "gasLimit":"79292",
                    "gasPrice":"32.595"
                }
            }
            """.trimIndent()

            coEvery { api.initiateOnchainWithdrawal(any()) } returns responseBody.fromJson()

            val repo = createRepository()
            val result = repo.initiateOnchainWithdrawal(
                sourceAccountId = StrigaAccountId("01c1f4e73d8b2587921c74e98951add0"),
                whitelistedAddressId = StrigaWhitelistedAddressId("5945b161-4534-4ada-bbb5-311e1869fe1b"),
                amount = BigInteger("10000000000000000"),
            )

            assertTrue(result is StrigaDataLayerResult.Success)
            result as StrigaDataLayerResult.Success<StrigaInitWithdrawalDetails>
            assertEquals("f8e7e49c-3559-4351-a2a4-e70b633744ec", result.value.challengeId.value)
            assertEquals("2023-06-27T13:37:53.356Z", result.value.dateExpires.toString())
            assertEquals("65b1c37c-686a-487b-81f4-8d0ea6dd0e53", result.value.transaction.userId)
            assertEquals("01c1f4e73d8b2587921c74e98951add0", result.value.transaction.sourceAccountId.value)
            assertEquals("a927c70c-3678-4d23-b54d-0261dda6bdbb", result.value.transaction.parentWalletId.value)
            assertEquals(StrigaNetworkCurrency.USDC, result.value.transaction.currency)
            assertEquals(BigInteger("2000"), result.value.transaction.amount)
            assertEquals(StrigaOnchainTxStatus.PENDING_2FA_CONFIRMATION, result.value.transaction.status)
            assertEquals(StrigaOnchainTxType.ON_CHAIN_WITHDRAWAL_INITIATED, result.value.transaction.txType)
            assertEquals(
                "0xcab7f66371b19b932b8bcecd8998a392026f3237",
                result.value.transaction.blockchainDestinationAddress
            )
            assertEquals("USD Coin Test (Goerli)", result.value.transaction.blockchainNetwork.name)
            assertEquals("ERC20", result.value.transaction.blockchainNetwork.type)
            assertEquals(
                "0x07865c6E87B9F70255377e024ace6630C1Eaa37F",
                result.value.transaction.blockchainNetwork.contractAddress
            )
            assertEquals(StrigaNetworkCurrency.USDC, result.value.transaction.transactionCurrency)
            assertEquals(BigInteger("521"), result.value.feeEstimate.totalFee)
            assertEquals(BigInteger("3058854447819900"), result.value.feeEstimate.networkFee)
            assertEquals(BigInteger("521"), result.value.feeEstimate.ourFee)
            assertEquals(BigInteger.ZERO, result.value.feeEstimate.theirFee)
            assertEquals(StrigaNetworkCurrency.USDC, result.value.feeEstimate.feeCurrency)
            assertEquals(BigInteger("79292"), result.value.feeEstimate.gasLimit)
            assertEquals(BigDecimal("32.595"), result.value.feeEstimate.gasPrice)
        }

    @Test
    fun `GIVEN enrich account response for EUR WHEN getFiatAccountDetails THEN check response is parsed ok`() =
        runTest {
            val responseBody = """
                {
                    "currency":"EUR",
                    "status":"ACTIVE",
                    "internalAccountId":"EUR09124356875233",
                    "bankCountry":"GB",
                    "bankAddress":"The Bower, 207-211 Old Street, London, England, EC1V 9NR",
                    "iban":"GB30SEOU19870010116943",
                    "bic":"SEOUGB21",
                    "accountNumber":"010116943",
                    "bankName":"Simulator Bank",
                    "bankAccountHolderName":"LORD VOLDEMORT",
                    "provider":"SIMULATOR",
                    "paymentType":null,
                    "domestic":false,
                    "routingCodeEntries":[],
                    "payInReference":null
                }
            """.trimIndent()

            coEvery { api.enrichFiatAccount(any()) } returns responseBody.fromJson()

            val repo = createRepository()
            val result = repo.getFiatAccountDetails(
                accountId = StrigaAccountId("01c1f4e73d8b2587921c74e98951add0"),
            )

            assertTrue(result is StrigaDataLayerResult.Success)
            result as StrigaDataLayerResult.Success<StrigaFiatAccountDetails>
            assertEquals("EUR", result.value.currency)
            assertEquals(StrigaFiatAccountStatus.ACTIVE, result.value.status)
            assertEquals("EUR09124356875233", result.value.internalAccountId)
            assertEquals("GB", result.value.bankCountry)
            assertEquals("The Bower, 207-211 Old Street, London, England, EC1V 9NR", result.value.bankAddress)
            assertEquals("GB30SEOU19870010116943", result.value.iban)
            assertEquals("SEOUGB21", result.value.bic)
            assertEquals("010116943", result.value.accountNumber)
            assertEquals("Simulator Bank", result.value.bankName)
            assertEquals("LORD VOLDEMORT", result.value.bankAccountHolderName)
            assertEquals("SIMULATOR", result.value.provider)
            assertEquals(null, result.value.paymentType)
            assertEquals(false, result.value.isDomesticAccount)
            assertEquals(emptyList<String>(), result.value.routingCodeEntries)
            assertEquals(null, result.value.payInReference)
        }

    private inline fun <reified T> String.fromJson(): T {
        return gson.fromJsonReified<T>(this) ?: error("Can't parse json")
    }

    private fun createRepository(): StrigaWalletRepository {
        return StrigaWalletRemoteRepository(
            api = api,
            mapper = StrigaWalletRepositoryMapper(),
            walletsMapper = mockk(),
            strigaUserIdProvider = mockk {
                every { getUserId() } returns userId
                every { getUserIdOrThrow() } returns userId
            }
        )
    }
}
