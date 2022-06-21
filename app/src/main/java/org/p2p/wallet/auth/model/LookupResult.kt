package org.p2p.wallet.auth.model

sealed interface LookupResult {
    class UsernameFound(val username: String) : LookupResult
    object UsernameNotFound : LookupResult
}
