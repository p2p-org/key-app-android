package org.p2p.wallet.auth.model

sealed class SignInResult {
    object WrongPin : SignInResult()
    object Success : SignInResult()
}
