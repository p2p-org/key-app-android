package org.p2p.wallet.striga.sms

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.core.utils.MillisSinceEpoch
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.model.StrigaApiErrorCode
import org.p2p.wallet.striga.model.StrigaApiErrorResponse
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toFailureResult
import org.p2p.wallet.striga.model.toSuccessResult
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.user.StrigaStorageContract
import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails
import org.p2p.wallet.striga.user.repository.StrigaUserRepository
import org.p2p.wallet.utils.mockInAppFeatureFlag

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaSmsInputInteractorTest {

    @MockK(relaxed = true)
    private lateinit var strigaUserRepository: StrigaUserRepository

    @MockK(relaxed = true)
    private lateinit var strigaSignupDataRepository: StrigaSignupDataLocalRepository

    @MockK(relaxed = true)
    private lateinit var countryCodeRepository: CountryCodeRepository

    @MockK(relaxed = true)
    private lateinit var inAppFeatureFlags: InAppFeatureFlags

    private val strigaStorage: StrigaStorageContract = object : StrigaStorageContract {
        override var userStatus: StrigaUserStatusDetails? = null
        override var smsExceededVerificationAttemptsMillis: MillisSinceEpoch = 0
        override var smsExceededResendAttemptsMillis: MillisSinceEpoch = 0
        override fun hideBanner(banner: StrigaKycStatusBanner) = Unit
        override fun isBannerHidden(banner: StrigaKycStatusBanner): Boolean = false
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { inAppFeatureFlags.strigaSmsVerificationMockFlag } returns mockInAppFeatureFlag(false)
    }

    @Test
    fun `GIVEN initial user status WHEN resendSms THEN check everything is fine`() = runTest {
        val expectedResult = Unit.toSuccessResult()
        coEvery { strigaUserRepository.resendSmsForVerifyPhoneNumber() } returns expectedResult

        val interactor = createInteractor()
        val result = interactor.resendSms()

        assertEquals(0, strigaStorage.smsExceededVerificationAttemptsMillis)
        assertEquals(0, strigaStorage.smsExceededResendAttemptsMillis)
        assertEquals(expectedResult, result)
    }

    @Test
    fun `GIVEN exceeded resend attempts WHEN resendSms THEN check error`() = runTest {
        val expectedResult: StrigaDataLayerResult<Unit> = StrigaDataLayerError.ApiServiceError(
            response = StrigaApiErrorResponse(
                httpStatus = 400,
                internalErrorCode = StrigaApiErrorCode.EXCEEDED_DAILY_RESEND_SMS_LIMIT,
                details = "EXCEEDED_DAILY_RESEND_SMS_LIMIT"
            )
        ).toFailureResult()

        val apiCaller: StrigaSmsApiCaller = mockk {
            coEvery { resendSms() } returns expectedResult
        }
        val interactor = createInteractor(apiCaller)
        val firstResult = interactor.resendSms()
        val secondResult = interactor.resendSms()

        assertTrue(firstResult is StrigaDataLayerResult.Failure)
        assertTrue(secondResult is StrigaDataLayerResult.Failure)
        assertTrue(firstResult.error is StrigaDataLayerError.ApiServiceError)
        assertTrue(secondResult.error is StrigaDataLayerError.ApiServiceError)
        assertEquals(
            StrigaApiErrorCode.EXCEEDED_DAILY_RESEND_SMS_LIMIT,
            (firstResult.error as StrigaDataLayerError.ApiServiceError).errorCode
        )
        assertEquals(
            StrigaApiErrorCode.EXCEEDED_DAILY_RESEND_SMS_LIMIT,
            (secondResult.error as StrigaDataLayerError.ApiServiceError).errorCode
        )

        assertEquals(0, strigaStorage.smsExceededVerificationAttemptsMillis)
        assertTrue(strigaStorage.smsExceededResendAttemptsMillis != 0L)

        coVerify(exactly = 1) { apiCaller.resendSms() }
    }

    @Test
    fun `GIVEN passed exceed timeout resend attempts WHEN resendSms THEN api is called`() = runTest {
        val expectedResult: StrigaDataLayerResult<Unit> = StrigaDataLayerError.ApiServiceError(
            response = StrigaApiErrorResponse(
                httpStatus = 400,
                internalErrorCode = StrigaApiErrorCode.EXCEEDED_DAILY_RESEND_SMS_LIMIT,
                details = "EXCEEDED_DAILY_RESEND_SMS_LIMIT"
            )
        ).toFailureResult()

        val apiCaller: StrigaSmsApiCaller = mockk {
            coEvery { resendSms() } returns expectedResult
        }

        val interactor = createInteractor(apiCaller)
        val firstResult = interactor.resendSms()
        val secondResult = interactor.resendSms()

        assertTrue(firstResult is StrigaDataLayerResult.Failure)
        assertTrue(secondResult is StrigaDataLayerResult.Failure)
        assertTrue(firstResult.error is StrigaDataLayerError.ApiServiceError)
        assertTrue(secondResult.error is StrigaDataLayerError.ApiServiceError)
        assertEquals(
            StrigaApiErrorCode.EXCEEDED_DAILY_RESEND_SMS_LIMIT,
            (firstResult.error as StrigaDataLayerError.ApiServiceError).errorCode
        )
        assertEquals(
            StrigaApiErrorCode.EXCEEDED_DAILY_RESEND_SMS_LIMIT,
            (secondResult.error as StrigaDataLayerError.ApiServiceError).errorCode
        )

        assertEquals(0, strigaStorage.smsExceededVerificationAttemptsMillis)
        assertTrue(strigaStorage.smsExceededResendAttemptsMillis != 0L)

        // simulate time has passed
        // shifting the clock back by one day
        strigaStorage.smsExceededResendAttemptsMillis = System.currentTimeMillis() - 1.days.inWholeMilliseconds

        interactor.resendSms()

        // checking that the api was twice
        // first - when we hadn't the timer
        // second - when the timer has passed
        coVerify(exactly = 2) { apiCaller.resendSms() }
    }

    private fun createInteractor(apiCaller: StrigaSmsApiCaller? = null): StrigaSmsInputInteractor {
        return StrigaSmsInputInteractor(
            strigaSignupDataRepository = strigaSignupDataRepository,
            phoneCodeRepository = countryCodeRepository,
            inAppFeatureFlags = inAppFeatureFlags,
            smsInputTimer = mockk(relaxed = true),
            strigaStorage = strigaStorage,
            smsApiCaller = apiCaller ?: mockk {
                coEvery { resendSms() } returns StrigaDataLayerResult.Success(Unit)
                coEvery { verifySms(any()) } returns StrigaDataLayerResult.Success(Unit)
            }
        )
    }
}
