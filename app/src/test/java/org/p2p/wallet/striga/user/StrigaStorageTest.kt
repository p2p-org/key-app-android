package org.p2p.wallet.striga.user

import android.content.SharedPreferences
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.math.BigDecimal
import org.p2p.core.BuildConfig
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.Base64String
import org.p2p.core.network.data.transactionerrors.RpcTransactionError
import org.p2p.core.network.data.transactionerrors.RpcTransactionErrorTypeAdapter
import org.p2p.core.network.data.transactionerrors.RpcTransactionInstructionErrorParser
import org.p2p.core.network.gson.Base58TypeAdapter
import org.p2p.core.network.gson.Base64TypeAdapter
import org.p2p.core.network.gson.BigDecimalTypeAdapter
import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails
import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus
import org.p2p.wallet.utils.assertThat
import org.p2p.wallet.utils.plantTimberToStdout

class StrigaStorageTest {

    init {
        plantTimberToStdout("StrigaStorageTest")
    }

    private val gson: Gson
        get() {
            val transactionErrorTypeAdapter = RpcTransactionErrorTypeAdapter(RpcTransactionInstructionErrorParser())
            return GsonBuilder()
                .apply { if (BuildConfig.DEBUG) setPrettyPrinting() }
                .registerTypeAdapter(BigDecimal::class.java, BigDecimalTypeAdapter)
                .registerTypeAdapter(Base58String::class.java, Base58TypeAdapter)
                .registerTypeAdapter(Base64String::class.java, Base64TypeAdapter)
                .registerTypeAdapter(RpcTransactionError::class.java, transactionErrorTypeAdapter)
                .setLenient()
                .disableHtmlEscaping()
                .create()
        }

    private val data = mutableMapOf<String, Any>()

    private val prefs: SharedPreferences = mockk() {
        every { getString(any(), any()) } answers {
            data.getOrDefault(firstArg(), secondArg<String?>()) as String?
        }
        every { getLong(any(), any()) } answers {
            data.getOrDefault(firstArg(), secondArg<Long?>()) as Long
        }
        every { edit() } returns mockk {
            val that = this
            every { apply() } returns Unit
            every { putLong(any(), any()) } answers {
                data[firstArg()] = secondArg()
                that
            }
            every { putString(any(), any()) } answers {
                data[firstArg()] = secondArg()
                that
            }
        }
    }

    @Test
    fun `GIVEN user status WHEN save it THEN the data is correctly saved as json`() {
        val expectedStatus = StrigaUserStatusDetails(
            userId = "userId",
            isEmailVerified = false,
            isMobileVerified = true,
            kycStatus = StrigaUserVerificationStatus.REJECTED
        )

        val storage = StrigaStorage(prefs, gson)
        storage.userStatus = expectedStatus

        storage.userStatus.assertThat()
            .isNotNull()
            .isEqualTo(expectedStatus)
    }
}
