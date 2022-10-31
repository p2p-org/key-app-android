package org.p2p.solanaj.core

class AccountCreationFailed(
    derivationPath: String,
    override val cause: Throwable?
) : Throwable(message = "Account creation with $derivationPath failed", cause)
