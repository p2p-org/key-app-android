package org.p2p.wallet.striga.wallet.repository

import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.api.response.StrigaUserWalletsResponse
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet
import org.p2p.wallet.striga.wallet.models.ids.StrigaWalletId
import org.p2p.wallet.utils.assertThat
import org.p2p.wallet.utils.fromJsonReified
import org.p2p.wallet.utils.stub

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaWalletRepositoryGetUserWalletTest {
    private val gson = Gson()
    private val api: StrigaWalletApi = mockk()
    private val userId = "65b1c37c-686a-487b-81f4-8d0ea6dd0e53"

    private var repository = StrigaWalletRemoteRepository(
        api = api,
        mapper = StrigaWalletRepositoryMapper(),
        walletsMapper = StrigaUserWalletsMapper(),
        strigaUserIdProvider = mockk {
            every { getUserIdOrThrow() }.returns(userId)
        }
    )

    @Before
    fun beforeEach() {
        repository = StrigaWalletRemoteRepository(
            api = api,
            mapper = StrigaWalletRepositoryMapper(),
            walletsMapper = StrigaUserWalletsMapper(),
            strigaUserIdProvider = mockk {
                every { getUserIdOrThrow() }.returns(userId)
            }
        )
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
        val actualResult: StrigaUserWallet? = repository.getUserWallet().unwrap()

        actualResult.assertThat()
            .isNotNull()
            .all {
                prop(StrigaUserWallet::walletId).isEqualTo(StrigaWalletId("3d57a943-8145-4183-8079-cd86b68d2993"))
                prop(StrigaUserWallet::userId).isEqualTo(userId)
                prop(StrigaUserWallet::eurAccount).isNotNull()
                prop(StrigaUserWallet::hasAvailableBalance).isTrue()

                prop(StrigaUserWallet::accounts).isNotEmpty()
            }
    }
}
