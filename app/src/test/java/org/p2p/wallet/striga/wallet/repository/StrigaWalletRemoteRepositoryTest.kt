package org.p2p.wallet.striga.wallet.repository

import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.core.utils.fromJsonReified
import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.api.response.StrigaUserWalletsResponse
import org.p2p.wallet.striga.wallet.models.StrigaBlockchainNetworkInfo
import org.p2p.wallet.striga.wallet.models.StrigaCryptoAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountStatus
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWalletId
import org.p2p.wallet.striga.wallet.repository.impl.StrigaWalletRemoteRepository
import org.p2p.wallet.striga.wallet.repository.mapper.StrigaWalletMapper
import org.p2p.wallet.utils.assertThat
import org.p2p.wallet.utils.createHttpException
import org.p2p.wallet.utils.fromJson
import org.p2p.wallet.utils.stub

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaWalletRemoteRepositoryTest {

    private val gson = Gson()
    private val api: StrigaWalletApi = mockk()
    private val userId = "65b1c37c-686a-487b-81f4-8d0ea6dd0e53"

    private lateinit var repository: StrigaWalletRepository

    @Before
    fun beforeEach() {
        repository = StrigaWalletRemoteRepository(
            api = api,
            mapper = StrigaWalletMapper(),
            strigaUserIdProvider = mockk {
                every { getUserId() } returns userId
                every { getUserIdOrThrow() } returns userId
            },
            cache = mockk {
                every { userWallet }.returns(null)
                every { fiatAccountDetails }.returns(null)
                every { cryptoAccountDetails }.returns(null)
                every { userWallet = any() }.returns(Unit)
                every { fiatAccountDetails = any() }.returns(Unit)
                every { cryptoAccountDetails = any() }.returns(Unit)
            },
        )
    }

    @Test
    fun `GIVEN enrich account response for EUR WHEN getFiatAccountDetails THEN check response is parsed ok`() =
        runTest {
            @Language("JSON")
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

            coEvery { api.enrichFiatAccount(any()) } returns responseBody.fromJson(gson)

            val result = repository.getFiatAccountDetails(
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

    @Test
    fun `GIVEN enrich account response for USDC WHEN getCryptoAccountDetails THEN check response is parsed ok`() =
        runTest {
            @Language("JSON")
            val responseBody = """
                {
                  "accountId": "01c1f4e73d8b2587921c74e98951add0",
                  "currency": "USDC",
                  "blockchainDepositAddress": "0x08E54EEE2EEFF2a4BF071746Fc1468BaC060Eb2a",
                  "blockchainNetwork": {
                    "name": "USD Coin Test (Goerli)",
                    "type": "ERC20",
                    "contractAddress": "0x07865c6E87B9F70255377e024ace6630C1Eaa37F"
                  }
                }
            """.trimIndent()

            coEvery { api.enrichCryptoAccount(any()) } returns responseBody.fromJson(gson)

            val result = repository.getCryptoAccountDetails(
                accountId = StrigaAccountId("01c1f4e73d8b2587921c74e98951add0"),
            )

            result.assertThat()
                .isInstanceOf(StrigaDataLayerResult.Success::class.java)

            result as StrigaDataLayerResult.Success<StrigaCryptoAccountDetails>
            result.value.assertThat()
                .all {
                    prop(StrigaCryptoAccountDetails::accountId)
                        .isEqualTo(StrigaAccountId("01c1f4e73d8b2587921c74e98951add0"))
                    prop(StrigaCryptoAccountDetails::currency)
                        .isEqualTo(StrigaNetworkCurrency.USDC)
                    prop(StrigaCryptoAccountDetails::depositAddress)
                        .isEqualTo("0x08E54EEE2EEFF2a4BF071746Fc1468BaC060Eb2a")
                    prop(StrigaCryptoAccountDetails::network).all {
                        prop(StrigaBlockchainNetworkInfo::name)
                            .isEqualTo("USD Coin Test (Goerli)")
                        prop(StrigaBlockchainNetworkInfo::type)
                            .isEqualTo("ERC20")
                        prop(StrigaBlockchainNetworkInfo::contractAddress)
                            .isEqualTo("0x07865c6E87B9F70255377e024ace6630C1Eaa37F")
                    }
                }
        }

    @Test
    fun `GIVEN 200 response WHEN get wallets THEN user wallet is gotten`() = runTest {
        // GIVEN
        @Language("JSON") val successResponse = """
{
  "wallets": [
    {
      "walletId": "3d57a943-8145-4183-8079-cd86b68d2993",
      "syncedOwnerId": "aa3534a1-d13d-4920-b023-97cb00d49bad",
      "ownerType": "CONSUMER",
      "createdAt": "2023-05-28T19:47:17.094Z",
      "comment": "DEFAULT",
      "accounts": {
        "EUR": {
          "accountId": "4dc6ecb29d74198e9e507f8025cad011",
          "parentWalletId": "3d57a943-8145-4183-8079-cd86b68d2993",
          "currency": "EUR",
          "ownerId": "aa3534a1-d13d-4920-b023-97cb00d49bad",
          "ownerType": "CONSUMER",
          "createdAt": "2023-05-28T19:47:17.077Z",
          "availableBalance": {
            "amount": "1888383",
            "currency": "cents"
          },
          "linkedCardId": "UNLINKED",
          "linkedBankAccountId": "EUR10112624134233",
          "status": "ACTIVE",
          "permissions": [
            "CUSTODY",
            "TRADE",
            "INTER",
            "INTRA"
          ],
          "enriched": true
        },
        "USDC": {
          "accountId": "140ecf6f979975c8e868d14038004b37",
          "parentWalletId": "3d57a943-8145-4183-8079-cd86b68d2993",
          "currency": "USDC",
          "ownerId": "aa3534a1-d13d-4920-b023-97cb00d49bad",
          "ownerType": "CONSUMER",
          "createdAt": "2023-05-28T19:47:17.078Z",
          "availableBalance": {
            "amount": "5889",
            "currency": "cents"
          },
          "linkedCardId": "UNLINKED",
          "blockchainDepositAddress": "0xF13607D9Ab2D98f6734Dc09e4CDE7dA515fe329c",
          "blockchainNetwork": {
            "name": "USD Coin Test (Goerli)",
            "type": "ERC20",
            "contractAddress": "0x07865c6E87B9F70255377e024ace6630C1Eaa37F"
          },
          "status": "ACTIVE",
          "permissions": [
            "CUSTODY",
            "TRADE",
            "INTER",
            "INTRA"
          ],
          "enriched": true
        }
      },
      "count": 1,
      "total": 1
    }
  ]
}
        """.trimIndent()
        val parsedResponse = gson.fromJsonReified<StrigaUserWalletsResponse>(successResponse)!!
        api.stub {
            coEvery { getUserWallets(any()) }.returns(parsedResponse)
        }
        // WHEN
        val actualResult: StrigaUserWallet = repository.getUserWallet().unwrap()

        actualResult.assertThat()
            .all {
                prop(StrigaUserWallet::walletId).isEqualTo(StrigaWalletId("3d57a943-8145-4183-8079-cd86b68d2993"))
                prop(StrigaUserWallet::userId).isEqualTo(userId)
                prop(StrigaUserWallet::eurAccount).isNotNull()
                prop(StrigaUserWallet::hasAvailableBalance).isTrue()

                prop(StrigaUserWallet::accounts).isNotEmpty()
            }
    }

    @Test
    fun `GIVEN 400 response WHEN get wallets THEN user wallet is not gotten`() = runTest {
        // GIVEN
        api.stub {
            coEvery { getUserWallets(any()) }.throws(createHttpException(400, "{}"))
        }
        // WHEN
        val actualResult = repository.getUserWallet()
        actualResult.assertThat()
            .isInstanceOf(StrigaDataLayerResult.Failure::class)
    }
}
