package org.p2p.wallet.striga.user

import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.google.gson.Gson
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.p2p.wallet.common.EncryptedSharedPreferences
import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails
import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus
import org.p2p.wallet.striga.user.storage.StrigaStorage
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountStatus
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet
import org.p2p.wallet.striga.wallet.models.ids.StrigaWalletId
import org.p2p.wallet.utils.InMemorySharedPreferences
import org.p2p.wallet.utils.TimberUnitTestInstance
import org.p2p.wallet.utils.assertThat
import org.p2p.wallet.utils.crypto.MockedKeyStoreWrapper

class StrigaStorageTest {

    companion object {
        @ClassRule
        @JvmField
        val timber = TimberUnitTestInstance(
            isEnabled = false,
            defaultTag = "StrigaStorageTest"
        )
    }

    private val prefs = EncryptedSharedPreferences(
        keyStoreWrapper = MockedKeyStoreWrapper.get(),
        sharedPreferences = InMemorySharedPreferences(),
        gson = Gson()
    )

    @Before
    fun setUp() {
        prefs.clear()
    }

    @Test
    fun `GIVEN user status WHEN save it THEN the data is correctly saved as json`() {
        val expectedStatus = StrigaUserStatusDetails(
            userId = "userId",
            isEmailVerified = false,
            isMobileVerified = true,
            kycStatus = StrigaUserVerificationStatus.REJECTED
        )

        val storage = StrigaStorage(prefs)
        storage.userStatus = expectedStatus

        storage.userStatus.assertThat()
            .isNotNull()
            .isEqualTo(expectedStatus)
    }

    @Test
    fun `GIVEN user wallet WHEN save it THEN the data is correctly saved as json`() {
        val expectedWallet = StrigaUserWallet(
            walletId = StrigaWalletId(value = "vituperata"),
            userId = "liber",
            accounts = listOf()
        )

        val storage = StrigaStorage(prefs)
        storage.userWallet = expectedWallet

        storage.userWallet.assertThat()
            .isNotNull()
            .isEqualTo(expectedWallet)
    }

    @Test
    fun `GIVEN user fiat details WHEN save it THEN the data is correctly saved as json`() {
        val expectedFiat = StrigaFiatAccountDetails(
            currency = "accusata",
            status = StrigaFiatAccountStatus.ACTIVE,
            internalAccountId = "vocibus",
            bankCountry = "Mexico",
            bankAddress = "donec",
            iban = "regione",
            bic = "diam",
            accountNumber = "debet",
            bankName = "Corine Abbott",
            bankAccountHolderName = "Rodger Mueller",
            provider = "error",
            paymentType = null,
            isDomesticAccount = false,
            routingCodeEntries = listOf(),
            payInReference = null
        )

        val storage = StrigaStorage(prefs)
        storage.fiatAccount = expectedFiat

        storage.fiatAccount.assertThat()
            .isNotNull()
            .isEqualTo(expectedFiat)
    }
}
