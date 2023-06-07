package org.p2p.wallet.striga.user.repository

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.striga.StrigaUserConstants
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaApiErrorCode
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.user.api.StrigaApi
import org.p2p.wallet.striga.user.api.StrigaCreateUserRequest
import org.p2p.wallet.striga.user.api.response.StrigaCreateUserResponse
import org.p2p.wallet.striga.user.model.StrigaUserInitialDetails
import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaUserRepositoryTest {

    @Test
    fun `GIVEN http error 400 WHEN createUser THEN check error is presented`() = runTest {
        val data = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "aaa"),
            StrigaSignupData(StrigaSignupDataType.PHONE_CODE_WITH_PLUS, "aaa"),
            StrigaSignupData(StrigaSignupDataType.PHONE_NUMBER, "aaa"),
            StrigaSignupData(StrigaSignupDataType.FIRST_NAME, "aaa"),
            StrigaSignupData(StrigaSignupDataType.LAST_NAME, "aaa"),
            StrigaSignupData(StrigaSignupDataType.DATE_OF_BIRTH, "05.05.2005"),
            StrigaSignupData(StrigaSignupDataType.COUNTRY_OF_BIRTH, "TUR"),
            StrigaSignupData(StrigaSignupDataType.OCCUPATION, "Loafer"),
            StrigaSignupData(StrigaSignupDataType.SOURCE_OF_FUNDS, "NOTHING"),
            StrigaSignupData(StrigaSignupDataType.COUNTRY, "TR"),
            StrigaSignupData(StrigaSignupDataType.CITY, "Antalya"),
            StrigaSignupData(StrigaSignupDataType.CITY_ADDRESS_LINE, "Hurma mahalesi, Ataturk prospect 1"),
            StrigaSignupData(StrigaSignupDataType.CITY_POSTAL_CODE, "056987"),
            StrigaSignupData(StrigaSignupDataType.CITY_STATE, "Antalya"),
        )
        val responseBody = """
            {
                "status": 400,
                "errorCode": ${StrigaApiErrorCode.USER_DOES_NOT_EXIST.code.toInt()}
            }
        """.trimIndent().toResponseBody("application/json".toMediaTypeOrNull())

        val api = mockk<StrigaApi>() {
            coEvery { createUser(any()) } throws HttpException(Response.error<StrigaCreateUserRequest>(400, responseBody))
        }
        val userIdProvider = mockk<StrigaUserIdProvider>()
        val mapper = StrigaUserRepositoryMapper()
        val repo = StrigaUserRemoteRepository(api, userIdProvider, mapper)

        var error: Throwable? = null
        var result: StrigaDataLayerResult<StrigaUserInitialDetails>? = null
        try {
            result = repo.createUser(data)
        } catch (e: Throwable) {
            error = e
        }
        assertNotNull(result, error?.message)
        assertNull(error)
        assertTrue(result is StrigaDataLayerResult.Failure)
        assertTrue(result.error is StrigaDataLayerError.ApiServiceError)
        assertEquals(
            StrigaApiErrorCode.USER_DOES_NOT_EXIST,
            (result.error as StrigaDataLayerError.ApiServiceError).response.errorCode
        )
    }

    @Test
    fun `GIVEN correct data and successful response WHEN createUser THEN check error is not presented`() = runTest {
        val data = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "aaa"),
            StrigaSignupData(StrigaSignupDataType.PHONE_CODE_WITH_PLUS, "bbb"),
            StrigaSignupData(StrigaSignupDataType.PHONE_NUMBER, "ccc"),
            StrigaSignupData(StrigaSignupDataType.FIRST_NAME, "ddd"),
            StrigaSignupData(StrigaSignupDataType.LAST_NAME, "eee"),
            StrigaSignupData(StrigaSignupDataType.DATE_OF_BIRTH, "05.05.2005"),
            StrigaSignupData(StrigaSignupDataType.COUNTRY_OF_BIRTH, "TUR"),
            StrigaSignupData(StrigaSignupDataType.OCCUPATION, "Loafer"),
            StrigaSignupData(StrigaSignupDataType.SOURCE_OF_FUNDS, "NOTHING"),
            StrigaSignupData(StrigaSignupDataType.COUNTRY, "TR"),
            StrigaSignupData(StrigaSignupDataType.CITY, "Antalya"),
            StrigaSignupData(StrigaSignupDataType.CITY_ADDRESS_LINE, "Hurma mahalesi, Ataturk avenue 1"),
            StrigaSignupData(StrigaSignupDataType.CITY_POSTAL_CODE, "056987"),
            StrigaSignupData(StrigaSignupDataType.CITY_STATE, "Antalya"),
        )
        val expectedUserId = "user_id_123456"
        val expectedUserEmail = "email@email.email"
        val expectedKycStatus = StrigaUserVerificationStatus.NOT_STARTED
        val responseData = StrigaCreateUserResponse(
            userId = expectedUserId,
            email = expectedUserEmail,
            kycDetails = StrigaCreateUserResponse.KycDataResponse(
                status = expectedKycStatus.name
            )
        )

        val createdData = slot<StrigaCreateUserRequest>()
        val api = mockk<StrigaApi> {
            coEvery { createUser(capture(createdData)) } returns responseData
        }
        val userIdProvider = mockk<StrigaUserIdProvider>()
        val mapper = StrigaUserRepositoryMapper()
        val repo = StrigaUserRemoteRepository(api, userIdProvider, mapper)

        // Check response
        var error: Throwable? = null
        var result: StrigaDataLayerResult<StrigaUserInitialDetails>? = null
        try {
            result = repo.createUser(data)
        } catch (e: Throwable) {
            error = e
        }
        assertNotNull(result)
        assertNull(error)
        assertTrue(result is StrigaDataLayerResult.Success<StrigaUserInitialDetails>)
        val userDetails = result.value
        assertEquals(expectedUserId, userDetails.userId)
        assertEquals(expectedUserEmail, userDetails.email)
        assertEquals(expectedKycStatus, userDetails.kycStatus.status)

        // Check request
        with(createdData.captured) {
            val dataMap = data.associate { it.type to it.value }
            assertEquals(dataMap[StrigaSignupDataType.EMAIL], userEmail)
            assertEquals(dataMap[StrigaSignupDataType.PHONE_CODE_WITH_PLUS], mobilePhoneDetails.countryCode)
            assertEquals(dataMap[StrigaSignupDataType.PHONE_NUMBER], mobilePhoneDetails.number)
            assertEquals(dataMap[StrigaSignupDataType.FIRST_NAME], firstName)
            assertEquals(dataMap[StrigaSignupDataType.LAST_NAME], lastName)
            val birthday = dataMap[StrigaSignupDataType.DATE_OF_BIRTH]!!.split(".")
            assertEquals(birthday[0].toInt(), dateOfBirth.day)
            assertEquals(birthday[1].toInt(), dateOfBirth.month)
            assertEquals(birthday[2].toInt(), dateOfBirth.year)
            assertEquals(dataMap[StrigaSignupDataType.COUNTRY_OF_BIRTH], placeOfBirth)
            assertEquals(dataMap[StrigaSignupDataType.OCCUPATION], occupation)
            assertEquals(dataMap[StrigaSignupDataType.SOURCE_OF_FUNDS], sourceOfFunds)
            assertEquals(dataMap[StrigaSignupDataType.COUNTRY], address.country)
            assertEquals(dataMap[StrigaSignupDataType.CITY], address.city)
            assertEquals(dataMap[StrigaSignupDataType.CITY_ADDRESS_LINE], address.addressLine1)
            assertEquals("", address.addressLine2)
            assertEquals(dataMap[StrigaSignupDataType.CITY_POSTAL_CODE], address.postalCode)
            assertEquals(dataMap[StrigaSignupDataType.CITY_STATE], address.state)

            // hardcoded
            assertEquals(StrigaUserConstants.EXPECTED_INCOMING_TX_YEARLY, expectedIncomingTxVolumeYearly)
            assertEquals(StrigaUserConstants.EXPECTED_OUTGOING_TX_YEARLY, expectedOutgoingTxVolumeYearly)
            assertEquals(StrigaUserConstants.SELF_PEP_DECLARATION, isSelfPepDeclaration)
            assertEquals(StrigaUserConstants.PURPOSE_OF_ACCOUNT, purposeOfAccount)
        }
    }
}
