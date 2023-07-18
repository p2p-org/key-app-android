package org.p2p.wallet.striga.sms.onramp

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
import org.p2p.wallet.striga.common.StrigaUserIdProvider
import org.p2p.wallet.striga.common.model.StrigaDataLayerError
import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.wallet.api.StrigaWalletApi
import org.p2p.wallet.striga.wallet.api.request.StrigaOnRampSmsResendRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaOnRampSmsVerifyRequest
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId
import org.p2p.wallet.striga.wallet.repository.StrigaWithdrawalsRepository
import org.p2p.wallet.striga.wallet.repository.impl.StrigaWithdrawalsRemoteRepository
import org.p2p.wallet.striga.wallet.repository.mapper.StrigaWithdrawalsMapper
import org.p2p.wallet.utils.assertThat

private val CHALLENGE_ID = StrigaWithdrawalChallengeId("challenge_id")

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaOnRampSmsApiCallerTest {

    @Test
    fun `GIVEN resend method WHEN resendSms THEN check resendSms api is called`() = runTest {
        val userId = "USER_ID"
        val userIdProvider = mockk<StrigaUserIdProvider>() {
            every { getUserIdOrThrow() } returns userId
            every { getUserId() } returns userId
        }
        val api = mockk<StrigaWalletApi>()
        val repository: StrigaWithdrawalsRepository = spyk(
            StrigaWithdrawalsRemoteRepository(
                api = api,
                mapper = StrigaWithdrawalsMapper(),
                strigaUserIdProvider = userIdProvider,
                ipAddressProvider = mockk {
                    every { getIpAddress() } returns "127.0.0.1"
                }
            )
        )
        val apiCaller = StrigaOnRampSmsApiCaller(CHALLENGE_ID, repository)

        apiCaller.resendSms()

        val requestSlot = slot<StrigaOnRampSmsResendRequest>()
        coVerify(exactly = 1) { api.withdrawalResendSms(capture(requestSlot)) }

        requestSlot.captured.assertThat()
            .isNotNull()
            .all {
                prop(StrigaOnRampSmsResendRequest::userId).isEqualTo(userId)
                prop(StrigaOnRampSmsResendRequest::challengeId).isEqualTo(CHALLENGE_ID.value)
            }
    }

    @Test
    fun `GIVEN resend method WHEN resendSms api call thrown an error THEN check resendSms returns error`() = runTest {
        val userId = "USER_ID"
        val userIdProvider = mockk<StrigaUserIdProvider> {
            every { getUserIdOrThrow() } returns userId
            every { getUserId() } returns userId
        }
        val api = mockk<StrigaWalletApi> {
            coEvery { withdrawalResendSms(any()) } throws IllegalStateException("expected error")
        }
        val repository: StrigaWithdrawalsRepository = spyk(
            StrigaWithdrawalsRemoteRepository(
                api = api,
                mapper = StrigaWithdrawalsMapper(),
                strigaUserIdProvider = userIdProvider,
                ipAddressProvider = mockk {
                    every { getIpAddress() } returns "127.0.0.1"
                }
            )
        )
        val apiCaller = StrigaOnRampSmsApiCaller(CHALLENGE_ID, repository)

        val result = apiCaller.resendSms()

        val requestSlot = slot<StrigaOnRampSmsResendRequest>()
        coVerify(exactly = 1) { api.withdrawalResendSms(capture(requestSlot)) }

        requestSlot.captured.assertThat()
            .isNotNull()
            .all {
                prop(StrigaOnRampSmsResendRequest::userId).isEqualTo(userId)
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
        val smsCode = "123456"
        val ipAddress = "127.0.0.1"
        val userIdProvider = mockk<StrigaUserIdProvider>() {
            every { getUserIdOrThrow() } returns userId
            every { getUserId() } returns userId
        }
        val api = mockk<StrigaWalletApi>()
        val repository: StrigaWithdrawalsRepository = spyk(
            StrigaWithdrawalsRemoteRepository(
                api = api,
                mapper = StrigaWithdrawalsMapper(),
                strigaUserIdProvider = userIdProvider,
                ipAddressProvider = mockk {
                    every { getIpAddress() } returns "127.0.0.1"
                }
            )
        )
        val apiCaller = StrigaOnRampSmsApiCaller(CHALLENGE_ID, repository)

        apiCaller.verifySms(smsCode)

        val requestSlot = slot<StrigaOnRampSmsVerifyRequest>()
        coVerify(exactly = 1) { api.withdrawalVerifySms(capture(requestSlot)) }

        requestSlot.captured.assertThat()
            .isNotNull()
            .all {
                prop(StrigaOnRampSmsVerifyRequest::userId).isEqualTo(userId)
                prop(StrigaOnRampSmsVerifyRequest::verificationCode).isEqualTo(smsCode)
                prop(StrigaOnRampSmsVerifyRequest::challengeId).isEqualTo(CHALLENGE_ID.value)
                prop(StrigaOnRampSmsVerifyRequest::ipAddress).isEqualTo(ipAddress)
            }
    }
}
