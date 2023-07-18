package org.p2p.wallet.striga.wallet.repository

import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.intellij.lang.annotations.Language
import org.junit.Assert
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.models.StrigaInitWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaOnchainTxStatus
import org.p2p.wallet.striga.wallet.models.StrigaOnchainTxType
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId
import org.p2p.wallet.striga.wallet.repository.impl.StrigaWithdrawalsRemoteRepository
import org.p2p.wallet.striga.wallet.repository.mapper.StrigaWithdrawalsMapper
import org.p2p.wallet.utils.fromJson

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaWithdrawalRemoteRepositoryTest {
    private val gson = Gson()
    private val api: StrigaWalletApi = mockk()
    private val userId = "65b1c37c-686a-487b-81f4-8d0ea6dd0e53"

    @Test
    fun `GIVEN init onchain withdrawal WHEN initiateOnchainWithdrawal THEN check response parsed correctly`() =
        runTest {
            // real response
            @Language("JSON")
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

            coEvery { api.initiateOnchainWithdrawal(any()) } returns responseBody.fromJson(gson)

            val repo = createRepository()
            val result = repo.initiateOnchainWithdrawal(
                sourceAccountId = StrigaAccountId("01c1f4e73d8b2587921c74e98951add0"),
                whitelistedAddressId = StrigaWhitelistedAddressId("5945b161-4534-4ada-bbb5-311e1869fe1b"),
                amountInUnits = BigInteger("10000000000000000"),
            )

            Assert.assertTrue(result is StrigaDataLayerResult.Success)
            result as StrigaDataLayerResult.Success<StrigaInitWithdrawalDetails>
            Assert.assertEquals("f8e7e49c-3559-4351-a2a4-e70b633744ec", result.value.challengeId.value)
            Assert.assertEquals("2023-06-27T13:37:53.356Z", result.value.dateExpires.toString())
            Assert.assertEquals("65b1c37c-686a-487b-81f4-8d0ea6dd0e53", result.value.transaction.userId)
            Assert.assertEquals("01c1f4e73d8b2587921c74e98951add0", result.value.transaction.sourceAccountId.value)
            Assert.assertEquals("a927c70c-3678-4d23-b54d-0261dda6bdbb", result.value.transaction.parentWalletId.value)
            Assert.assertEquals(StrigaNetworkCurrency.USDC, result.value.transaction.currency)
            Assert.assertEquals(BigInteger("2000"), result.value.transaction.amountInUnits)
            Assert.assertEquals(StrigaOnchainTxStatus.PENDING_2FA_CONFIRMATION, result.value.transaction.status)
            Assert.assertEquals(StrigaOnchainTxType.ON_CHAIN_WITHDRAWAL_INITIATED, result.value.transaction.txType)
            Assert.assertEquals(
                "0xcab7f66371b19b932b8bcecd8998a392026f3237",
                result.value.transaction.blockchainDestinationAddress
            )
            Assert.assertEquals("USD Coin Test (Goerli)", result.value.transaction.blockchainNetwork.name)
            Assert.assertEquals("ERC20", result.value.transaction.blockchainNetwork.type)
            Assert.assertEquals(
                "0x07865c6E87B9F70255377e024ace6630C1Eaa37F",
                result.value.transaction.blockchainNetwork.contractAddress
            )
            Assert.assertEquals(StrigaNetworkCurrency.USDC, result.value.transaction.transactionCurrency)
            Assert.assertEquals(BigInteger("521"), result.value.feeEstimate.totalFee)
            Assert.assertEquals(BigInteger("3058854447819900"), result.value.feeEstimate.networkFee)
            Assert.assertEquals(BigInteger("521"), result.value.feeEstimate.ourFee)
            Assert.assertEquals(BigInteger.ZERO, result.value.feeEstimate.theirFee)
            Assert.assertEquals(StrigaNetworkCurrency.USDC, result.value.feeEstimate.feeCurrency)
            Assert.assertEquals(BigInteger("79292"), result.value.feeEstimate.gasLimit)
            Assert.assertEquals(BigDecimal("32.595"), result.value.feeEstimate.gasPrice)
        }

    private fun createRepository(): StrigaWithdrawalsRepository {
        return StrigaWithdrawalsRemoteRepository(
            api = api,
            mapper = StrigaWithdrawalsMapper(),
            strigaUserIdProvider = mockk {
                every { getUserId() } returns userId
                every { getUserIdOrThrow() } returns userId
            },
            ipAddressProvider = mockk {
                every { getIpAddress() } returns "127.0.0.1"
            }
        )
    }
}
