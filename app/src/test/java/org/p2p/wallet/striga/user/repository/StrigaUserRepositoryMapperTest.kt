package org.p2p.wallet.striga.user.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.user.api.response.StrigaCreateUserResponse
import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaUserRepositoryMapperTest {

    @Test
    fun `GIVEN invalid striga signup data WHEN toNetwork THEN error occurred`() = runTest {
        val data = listOf<StrigaSignupData>()
        val mapper = StrigaUserRepositoryMapper()

        var error: Throwable? = null
        try {
            mapper.toNetwork(data)
        } catch (e: Throwable) {
            error = e
        }
        assertNotNull(error)
    }

    @Test
    fun `GIVEN given wrong birthday format WHEN toNetwork THEN error occurred`() = runTest {
        val data = listOf(
            StrigaSignupData(StrigaSignupDataType.DATE_OF_BIRTH, "2020-01-01")
        )
        val mapper = StrigaUserRepositoryMapper()

        var error: Throwable? = null
        try {
            mapper.toNetwork(data)
        } catch (e: IllegalStateException) {
            error = e
        }
        assertNotNull(error)
    }

    @Test
    fun `GIVEN correct signup data without placeOfBirth WHEN toNetwork THEN check is error occurred`() = runTest {
        val data = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "aaa"),
            StrigaSignupData(StrigaSignupDataType.PHONE_CODE, "aaa"),
            StrigaSignupData(StrigaSignupDataType.PHONE_NUMBER, "aaa"),
            StrigaSignupData(StrigaSignupDataType.FIRST_NAME, "aaa"),
            StrigaSignupData(StrigaSignupDataType.LAST_NAME, "aaa"),
            StrigaSignupData(StrigaSignupDataType.DATE_OF_BIRTH, "05.05.2005"),
            StrigaSignupData(StrigaSignupDataType.OCCUPATION, "Loafer"),
            StrigaSignupData(StrigaSignupDataType.SOURCE_OF_FUNDS, "NOTHING"),
            StrigaSignupData(StrigaSignupDataType.COUNTRY_ALPHA_2, "TR"),
            StrigaSignupData(StrigaSignupDataType.CITY, "Antalya"),
            StrigaSignupData(StrigaSignupDataType.CITY_ADDRESS_LINE, "Hurma mahalesi, Ataturk prospect 1"),
            StrigaSignupData(StrigaSignupDataType.CITY_POSTAL_CODE, "056987"),
            StrigaSignupData(StrigaSignupDataType.CITY_STATE, "Antalya"),
        )
        val mapper = StrigaUserRepositoryMapper()

        var error: Throwable? = null
        try {
            mapper.toNetwork(data)
        } catch (e: StrigaDataLayerError) {
            error = e
        }
        assertNotNull(error)
        error as StrigaDataLayerError.InternalError
        assertEquals("Key COUNTRY_OF_BIRTH_ALPHA_3 not found in map", error.message)
    }

    @Test
    fun `GIVEN correct signup data WHEN toNetwork THEN everything is ok`() = runTest {
        val data = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "aaa"),
            StrigaSignupData(StrigaSignupDataType.PHONE_CODE, "aaa"),
            StrigaSignupData(StrigaSignupDataType.PHONE_NUMBER, "aaa"),
            StrigaSignupData(StrigaSignupDataType.FIRST_NAME, "aaa"),
            StrigaSignupData(StrigaSignupDataType.LAST_NAME, "aaa"),
            StrigaSignupData(StrigaSignupDataType.DATE_OF_BIRTH, "05.05.2005"),
            StrigaSignupData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, "TUR"),
            StrigaSignupData(StrigaSignupDataType.OCCUPATION, "Loafer"),
            StrigaSignupData(StrigaSignupDataType.SOURCE_OF_FUNDS, "NOTHING"),
            StrigaSignupData(StrigaSignupDataType.COUNTRY_ALPHA_2, "TR"),
            StrigaSignupData(StrigaSignupDataType.CITY, "Antalya"),
            StrigaSignupData(StrigaSignupDataType.CITY_ADDRESS_LINE, "Hurma mahalesi, Ataturk prospect 1"),
            StrigaSignupData(StrigaSignupDataType.CITY_POSTAL_CODE, "056987"),
            StrigaSignupData(StrigaSignupDataType.CITY_STATE, "Antalya"),
        )
        val mapper = StrigaUserRepositoryMapper()

        var error: Throwable? = null
        try {
            mapper.toNetwork(data)
        } catch (e: IllegalStateException) {
            error = e
        }
        assertNull(error)
    }

    @Test
    fun `GIVEN create user response with unknown status WHEN fromNetwork THEN check MappingError`() {
        val mapper = StrigaUserRepositoryMapper()
        val response = StrigaCreateUserResponse(
            userId = "user_id",
            email = "email",
            kycDetails = StrigaCreateUserResponse.KycDataResponse(
                status = "some_status"
            )
        )
        var error: Throwable? = null
        try {
            mapper.fromNetwork(response)
        } catch (e: StrigaDataLayerError) {
            error = e
        }
        assertNotNull(error)
        assertTrue(error is StrigaDataLayerError.InternalError)
        error as StrigaDataLayerError.InternalError
        assertEquals("StrigaUserInitialDetails mapping failed", error.message)
        assertEquals("Unsupported KYC status: some_status", error.cause?.message)
    }

    @Test
    fun `GIVEN valid create user response WHEN fromNetwork THEN check everything is ok`() {
        val mapper = StrigaUserRepositoryMapper()
        val response = StrigaCreateUserResponse(
            userId = "user_id",
            email = "email",
            kycDetails = StrigaCreateUserResponse.KycDataResponse(
                status = StrigaUserVerificationStatus.NOT_STARTED.toString()
            )
        )

        val responseData = mapper.fromNetwork(response)
        assertEquals("user_id", responseData.userId)
        assertEquals("email", responseData.email)
        assertEquals(StrigaUserVerificationStatus.NOT_STARTED, responseData.kycStatus.status)
    }
}
