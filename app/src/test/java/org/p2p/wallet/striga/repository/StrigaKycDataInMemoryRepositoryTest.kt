package org.p2p.wallet.striga.repository

import assertk.assertThat
import assertk.assertions.isNotEmpty
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.p2p.wallet.R
import org.p2p.wallet.striga.signup.presetpicker.repository.StrigaPresetDataInMemoryRepository
import org.p2p.wallet.striga.signup.presetpicker.repository.StrigaPresetDataLocalRepository
import org.p2p.wallet.utils.getTestRawResource

class StrigaKycDataInMemoryRepositoryTest {

    private val repository: StrigaPresetDataLocalRepository = StrigaPresetDataInMemoryRepository(
        resources = mockk {
            every { openRawResource(R.raw.striga_occupation_values) }
                .returns(getTestRawResource("striga_occupation_values.json"))
            every { openRawResource(R.raw.striga_source_of_funds_values) }
                .returns(getTestRawResource("striga_source_of_funds_values.json"))
        },
        gson = Gson()
    )

    @Test
    fun `GIVEN valid occupation json WHEN parse THEN list is valid`() {
        val result = repository.getOccupationValuesList()
        assertThat(result).isNotEmpty()
    }

    @Test
    fun `GIVEN valid source of funds json WHEN parse THEN list is valid`() {
        val result = repository.getSourceOfFundsList()
        assertThat(result).isNotEmpty()
    }
}
