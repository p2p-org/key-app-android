package org.p2p.wallet.striga.model

import assertk.all
import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.hasSameSizeAs
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
import com.google.gson.Gson
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import retrofit2.HttpException
import java.net.HttpURLConnection
import org.p2p.wallet.utils.createHttpException

class StrigaDataLayerHttpError400And409ParserTest {

    private val gson = Gson()

    private var parser = StrigaDataLayerHttpErrorParser()

    private fun generateStrigaHttpException(httpCode: Int, strigaCode: String): HttpException {
        return createHttpException(
            code = HttpURLConnection.HTTP_BAD_REQUEST,
            errorBody = gson.toJson(
                mapOf(
                    "status" to httpCode,
                    "errorCode" to strigaCode,
                    "errorDetails" to null
                )
            )
        )
    }

    @BeforeEach
    fun beforeEach() {
        parser = StrigaDataLayerHttpErrorParser()
    }

    @Test
    fun `GIVEN all 400 or 409 http error jsons WHEN parse THEN get non-unknown error code`() {
        // GIVEN
        val errors: List<HttpException> = StrigaApiErrorCode.values()
            .toSet()
            .minus(StrigaApiErrorCode.UNKNOWN)
            .flatMap {
                listOf(
                    generateStrigaHttpException(HttpURLConnection.HTTP_BAD_REQUEST, it.code),
                    generateStrigaHttpException(HttpURLConnection.HTTP_CONFLICT, it.code)
                )
            }

        // WHEN
        val actualParsedErrors = errors.mapNotNull(parser::parse)

        // THEN
        assertThat(actualParsedErrors).all {
            hasSameSizeAs(errors)
            each { eachError ->
                eachError.isInstanceOf(StrigaDataLayerError.ApiServiceError::class)
                    .transform { it.response.errorCode }
                    .isNotEqualTo(StrigaApiErrorCode.UNKNOWN)
            }
        }
    }

    @Test
    fun `GIVEN 400 or 409 http error jsons with unknown code WHEN parse THEN get unknown error code`() {
        // GIVEN
        val errors = listOf(
            generateStrigaHttpException(
                httpCode = HttpURLConnection.HTTP_BAD_REQUEST,
                strigaCode = "9999"
            ),
            generateStrigaHttpException(
                httpCode = HttpURLConnection.HTTP_CONFLICT,
                strigaCode = "9999"
            )
        )

        // WHEN
        val actualParsedErrors = errors.mapNotNull(parser::parse)

        // THEN
        assertThat(actualParsedErrors).all {
            hasSameSizeAs(errors)
            each { error ->
                error.isInstanceOf(StrigaDataLayerError.ApiServiceError::class)
                    .transform { it.response.errorCode }
                    .isEqualTo(StrigaApiErrorCode.UNKNOWN)
            }
        }
    }
}
