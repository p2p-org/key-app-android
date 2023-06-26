package org.p2p.wallet.auth.username.repository

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.auth.username.repository.model.UsernameDetails
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.utils.Constants

class UsernameParserTest {

    private val domainFeatureToggle = mockk<UsernameDomainFeatureToggle> {
        every { value }.returns(".key")
    }

    private val usernameParser = UsernameParser(domainFeatureToggle)

    @Test
    fun `GIVEN different usernames with domains WHEN parse THEN domain and username parsed successfully`() {
        // GIVEN
        val ownerAddress = Constants.SOL_MINT.toBase58Instance()
        val usernamesToDomains = listOf(
            "alla" to ".key",
            "alla" to ".key.sol",
            "alla" to ".sol",
            "alla" to ".p2p.sol",
            "alla" to ".p2p.sol.key.sol",
        )
        // WHEN
        val parsedUsernames = usernamesToDomains.map {
            val usernameWithDomain = it.first + it.second
            usernameParser.parse(ownerAddress, usernameWithDomain)
        }

        // THEN
        parsedUsernames.onEachIndexed { index, usernameDetails ->
            val (expectedUsername, expectedDomain) = usernamesToDomains[index]
            assertThat(usernameDetails, "Check parse result for $expectedUsername").all {
                prop(UsernameDetails::ownerAddress).isEqualTo(ownerAddress)

                val username = prop(UsernameDetails::username)
                username.prop(Username::value).isEqualTo(expectedUsername)
                username.prop(Username::domainPrefix).isEqualTo(expectedDomain)
                username.prop(Username::fullUsername).isEqualTo(expectedUsername + expectedDomain)
            }
        }
    }

    @Test
    fun `GIVEN different usernames without domains WHEN parse THEN parsed successfully with default value`() {
        // GIVEN
        val ownerAddress = Constants.SOL_MINT.toBase58Instance()
        val usernamesToDomains = listOf(
            "alla" to "",
            "alla" to ".sol",
        )
        // WHEN
        val parsedUsernames = usernamesToDomains.map {
            val usernameWithDomain = it.first + it.second
            usernameParser.parse(ownerAddress, usernameWithDomain)
        }

        parsedUsernames.onEachIndexed { index, usernameDetails ->
            val (expectedUsername, expectedDomain) = usernamesToDomains[index].let { (username, domain) ->
                username to domain.ifBlank(domainFeatureToggle::value)
            }

            // THEN
            assertThat(usernameDetails, "Check parse result for $expectedUsername").all {
                prop(UsernameDetails::ownerAddress).isEqualTo(ownerAddress)

                val username = prop(UsernameDetails::username)
                username.prop(Username::value).isEqualTo(expectedUsername)
                username.prop(Username::domainPrefix).isEqualTo(expectedDomain)
                username.prop(Username::fullUsername).isEqualTo(expectedUsername + expectedDomain)
            }
        }
    }
}
