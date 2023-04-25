package org.p2p.wallet.utils

import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle

class UsernameFormatter(
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle
) {
    fun format(username: String): String {
        return if (username.endsWith(usernameDomainFeatureToggle.value)) {
            "@$username"
        } else {
            username
        }
    }

    fun formatOrNull(username: String?): String? {
        if (username == null) return null
        return format(username)
    }
}
