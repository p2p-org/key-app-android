package org.p2p.wallet.auth.username.repository

import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.auth.username.repository.model.UsernameDetails
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.core.crypto.Base58String

/**
 * @see [org.p2p.wallet.auth.username.repository.UsernameParserTest]
 */
class UsernameParser(
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle
) {
    /**
     * @param anyUsername - username with domain prefix or not
     */
    fun parse(ownerAddress: Base58String, anyUsername: String): UsernameDetails {
        val firstDotIndex = anyUsername.indexOf('.')
        val (rawUsername, domain) = if (firstDotIndex > 0) {
            anyUsername.substring(0, firstDotIndex) to anyUsername.substring(firstDotIndex, anyUsername.length)
        } else {
            anyUsername to usernameDomainFeatureToggle.value
        }
        val username = Username(value = rawUsername, domainPrefix = domain)
        return UsernameDetails(ownerAddress, username)
    }
}
