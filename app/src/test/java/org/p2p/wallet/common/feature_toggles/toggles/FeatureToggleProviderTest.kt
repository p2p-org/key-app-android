package org.p2p.wallet.common.feature_toggles.toggles

import assertk.assertions.isInstanceOf
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.p2p.wallet.common.feature_toggles.toggles.remote.RemoteFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.utils.assertThat

class FeatureToggleProviderTest {
    private lateinit var ftProvider: FeatureToggleProvider

    private val mockedToggles: List<RemoteFeatureToggle<*>> = listOf(
        mockk<StrigaSignupEnabledFeatureToggle>(relaxed = true),
        mockk<UsernameDomainFeatureToggle>(relaxed = true),
    )

    @Before
    fun setUp() {
        ftProvider = FeatureToggleProvider(mockedToggles)
    }

    @Test
    fun `WHEN get for different FT is called THEN return FT with correct type`(){
        // WHEN
        val strigaFt =  ftProvider.getFeatureToggle(StrigaSignupEnabledFeatureToggle::class)
        val strigaFt2 = ftProvider.getFeatureToggle<StrigaSignupEnabledFeatureToggle>()
        val strigaFt3 = ftProvider[StrigaSignupEnabledFeatureToggle::class]

        val usernameFt =  ftProvider.getFeatureToggle(UsernameDomainFeatureToggle::class)
        val usernameFt2 = ftProvider.getFeatureToggle<UsernameDomainFeatureToggle>()
        val usernameFt3 = ftProvider[UsernameDomainFeatureToggle::class]

        // THEN
        strigaFt.assertThat().isInstanceOf(StrigaSignupEnabledFeatureToggle::class)
        strigaFt2.assertThat().isInstanceOf(StrigaSignupEnabledFeatureToggle::class)
        strigaFt3.assertThat().isInstanceOf(StrigaSignupEnabledFeatureToggle::class)

        usernameFt.assertThat().isInstanceOf(UsernameDomainFeatureToggle::class)
        usernameFt2.assertThat().isInstanceOf(UsernameDomainFeatureToggle::class)
        usernameFt3.assertThat().isInstanceOf(UsernameDomainFeatureToggle::class)
    }



}
