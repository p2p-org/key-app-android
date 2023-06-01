package org.p2p.wallet.striga.model

import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import java.net.HttpURLConnection
import org.p2p.wallet.utils.createHttpException
import org.p2p.wallet.utils.emptyString

class StrigaDataLayerHttpError503ParserTest {
    private var parser = StrigaDataLayerHttpErrorParser()

    @BeforeEach
    fun beforeEach() {
        parser = StrigaDataLayerHttpErrorParser()
    }

    @Test
    fun `GIVEN 503 http error WHEN parse THEN get correct error`() {
        // GIVEN
        val internalServiceHttp = createHttpException(HttpURLConnection.HTTP_UNAVAILABLE, emptyString())

        // WHEN
        val actualError = parser.parse(internalServiceHttp)

        // THEN
        assertThat(actualError)
            .isNotNull()
            .isInstanceOf(StrigaDataLayerError.ApiServiceUnavailable::class)
    }
}
