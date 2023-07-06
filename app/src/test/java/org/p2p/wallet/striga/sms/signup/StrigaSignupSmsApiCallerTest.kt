package org.p2p.wallet.striga.sms.signup

import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.prop
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.user.api.StrigaApi
import org.p2p.wallet.striga.user.api.request.StrigaResendSmsRequest
import org.p2p.wallet.striga.user.api.request.StrigaVerifyMobileNumberRequest
import org.p2p.wallet.striga.user.repository.StrigaUserRemoteRepository
import org.p2p.wallet.striga.user.repository.StrigaUserRepository
import org.p2p.wallet.striga.user.repository.StrigaUserRepositoryMapper
import org.p2p.wallet.utils.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaSignupSmsApiCallerTest {

    @Test
    fun `GIVEN resend method WHEN resendSms THEN check resendSms api is called`() = runTest {
        val userId = "USER_ID"
        val userIdProvider = mockk<StrigaUserIdProvider>() {
            every { getUserIdOrThrow() } returns userId
            every { getUserId() } returns userId
        }
        val api = mockk<StrigaApi>()
        val repository: StrigaUserRepository = spyk(
            StrigaUserRemoteRepository(
                api = api,
                strigaUserIdProvider = userIdProvider,
                mapper = StrigaUserRepositoryMapper()
            )
        )
        val apiCaller = StrigaSignupSmsApiCaller(repository)

        apiCaller.resendSms()

        val requestSlot = slot<StrigaResendSmsRequest>()
        coVerify(exactly = 1) { api.resendSms(capture(requestSlot)) }

        requestSlot.captured.assertThat()
            .isNotNull()
            .all {
                prop(StrigaResendSmsRequest::userId).isEqualTo(userId)
            }
    }

    @Test
    fun `GIVEN resend method WHEN resendSms api call thrown an error THEN check resendSms returns error`() = runTest {
        val userId = "USER_ID"
        val userIdProvider = mockk<StrigaUserIdProvider>() {
            every { getUserIdOrThrow() } returns userId
            every { getUserId() } returns userId
        }
        val api = mockk<StrigaApi>() {
            coEvery { resendSms(any()) } throws IllegalStateException("expected error")
        }
        val repository: StrigaUserRepository = spyk(
            StrigaUserRemoteRepository(
                api = api,
                strigaUserIdProvider = userIdProvider,
                mapper = StrigaUserRepositoryMapper()
            )
        )
        val apiCaller = StrigaSignupSmsApiCaller(repository)

        val result = apiCaller.resendSms()

        val requestSlot = slot<StrigaResendSmsRequest>()
        coVerify(exactly = 1) { api.resendSms(capture(requestSlot)) }

        requestSlot.captured.assertThat()
            .isNotNull()
            .all {
                prop(StrigaResendSmsRequest::userId).isEqualTo(userId)
            }

        result.assertThat()
            .isInstanceOf(StrigaDataLayerResult.Failure::class.java)

        result as StrigaDataLayerResult.Failure
        result.assertThat()
            .all {
                prop(StrigaDataLayerResult.Failure<Unit>::error)
                    .isInstanceOf(StrigaDataLayerError.InternalError::class.java)
            }

        result.error.message.assertThat()
            .isEqualTo("expected error")
    }

    @Test
    fun `GIVEN verify method WHEN verifySms THEN check verifySms api is called`() = runTest {
        val userId = "USER_ID"
        val userIdProvider = mockk<StrigaUserIdProvider>() {
            every { getUserIdOrThrow() } returns userId
            every { getUserId() } returns userId
        }
        val api = mockk<StrigaApi>()
        val repository: StrigaUserRepository = spyk(
            StrigaUserRemoteRepository(
                api = api,
                strigaUserIdProvider = userIdProvider,
                mapper = StrigaUserRepositoryMapper()
            )
        )
        val apiCaller = StrigaSignupSmsApiCaller(repository)

        apiCaller.verifySms("123456")

        val requestSlot = slot<StrigaVerifyMobileNumberRequest>()
        coVerify(exactly = 1) { api.verifyMobileNumber(capture(requestSlot)) }

        requestSlot.captured.assertThat()
            .isNotNull()
            .all {
                prop(StrigaVerifyMobileNumberRequest::userId).isEqualTo(userId)
                prop(StrigaVerifyMobileNumberRequest::verificationCode).isEqualTo("123456")
            }
    }
}
