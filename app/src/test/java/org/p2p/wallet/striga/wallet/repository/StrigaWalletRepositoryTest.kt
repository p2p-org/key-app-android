package org.p2p.wallet.striga.wallet.repository

import com.google.gson.Gson
import io.mockk.coEvery
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
import org.p2p.wallet.striga.wallet.models.StrigaInitiateOnchainWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaOnchainTxStatus
import org.p2p.wallet.striga.wallet.models.StrigaOnchainTxType
import org.p2p.wallet.striga.wallet.models.StrigaWhitelistedAddressItem
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId
import org.p2p.wallet.utils.fromJsonReified

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaWalletRepositoryTest {

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
            assertEquals("5945b161-4534-4ada-bbb5-311e1869fe1b", result.value.first().id.value)
            assertEquals("0xcab7f66371b19b932b8bcecd8998a392026f3237", result.value.first().address)
            assertEquals(StrigaNetworkCurrency.USDC, result.value.first().currency)
            assertEquals(StrigaWhitelistedAddressItem.Status.ACTIVATED, result.value.first().status)
            assertEquals("USD Coin Test (Goerli)", result.value.first().network.name)
            assertEquals("ERC20", result.value.first().network.type)
            assertEquals("0x07865c6E87B9F70255377e024ace6630C1Eaa37F", result.value.first().network.contractAddress)
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
        val result = repo.addWhitelistedAddress(
            userId = userId,
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
            // todo: this is not actually a real response, it's taken from documentation
            val responseBody = """
            {
                "challengeId": "eaec4a27-d78d-4f49-80bf-9c1ecba98853",
                "dateExpires": "2023-03-30T05:21:47.402Z",
                "transaction": {
                    "syncedOwnerId": "51a2ed48-3b70-4775-b549-0d7e4850b64d",
                    "sourceAccountId": "9c73b2f8a7c4e567c0460ef83c309ce1",
                    "parentWalletId": "2c24c517-c682-4472-bbde-627e4a26fcf8",
                    "currency": "ETH",
                    "amount": "10000000000000000",
                    "status": "PENDING_2FA_CONFIRMATION",
                    "txType": "ON_CHAIN_WITHDRAWAL_INITIATED",
                    "blockchainDestinationAddress": "0x6475C4E02248E463fDBbF2D3fB436aFCa9c56DbD",
                    "blockchainNetwork": {
                        "name": "Ethereum Test (Goerli)"
                    },
                    "transactionCurrency": "ETH"
                },
                "feeEstimate": {
                    "totalFee": "948640405755000",
                    "networkFee": "948640405755000",
                    "ourFee": "948640405755000",
                    "theirFee": "0",
                    "feeCurrency": "ETH",
                    "gasLimit": "21000",
                    "gasPrice": "21.044"
                }
            }
            """.trimIndent()

            coEvery { api.initiateOnchainWithdrawal(any()) } returns responseBody.fromJson()

            val repo = createRepository()
            val result = repo.initiateOnchainWithdrawal(
                userId = userId,
                sourceAccountId = StrigaAccountId("01c1f4e73d8b2587921c74e98951add0"),
                whitelistedAddressId = StrigaWhitelistedAddressId("5945b161-4534-4ada-bbb5-311e1869fe1b"),
                amount = BigInteger("10000000000000000"),
            )

            assertTrue(result is StrigaDataLayerResult.Success)
            result as StrigaDataLayerResult.Success<StrigaInitiateOnchainWithdrawalDetails>
            assertEquals("eaec4a27-d78d-4f49-80bf-9c1ecba98853", result.value.challengeId.value)
            assertEquals("2023-03-30T05:21:47.402Z", result.value.dateExpires.toString())
            assertEquals("51a2ed48-3b70-4775-b549-0d7e4850b64d", result.value.transaction.userId)
            assertEquals("9c73b2f8a7c4e567c0460ef83c309ce1", result.value.transaction.sourceAccountId.value)
            assertEquals("2c24c517-c682-4472-bbde-627e4a26fcf8", result.value.transaction.parentWalletId.value)
            assertEquals(StrigaNetworkCurrency.ETH, result.value.transaction.currency)
            assertEquals(BigInteger("10000000000000000"), result.value.transaction.amount)
            assertEquals(StrigaOnchainTxStatus.PENDING_2FA_CONFIRMATION, result.value.transaction.status)
            assertEquals(StrigaOnchainTxType.ON_CHAIN_WITHDRAWAL_INITIATED, result.value.transaction.txType)
            assertEquals("0x6475C4E02248E463fDBbF2D3fB436aFCa9c56DbD", result.value.transaction.blockchainDestinationAddress)
            assertEquals("Ethereum Test (Goerli)", result.value.transaction.blockchainNetwork.name)
            assertEquals(StrigaNetworkCurrency.ETH, result.value.transaction.transactionCurrency)
            assertEquals(BigInteger("948640405755000"), result.value.feeEstimate.totalFee)
            assertEquals(BigInteger("948640405755000"), result.value.feeEstimate.networkFee)
            assertEquals(BigInteger("948640405755000"), result.value.feeEstimate.ourFee)
            assertEquals(BigInteger.ZERO, result.value.feeEstimate.theirFee)
            assertEquals(StrigaNetworkCurrency.ETH, result.value.feeEstimate.feeCurrency)
            assertEquals(BigInteger("21000"), result.value.feeEstimate.gasLimit)
            assertEquals(BigDecimal("21.044"), result.value.feeEstimate.gasPrice)
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
                userId,
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
            assertEquals(false, result.value.domestic)
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
        )
    }
}
