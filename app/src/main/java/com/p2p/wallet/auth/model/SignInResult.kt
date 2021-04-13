package com.p2p.wallet.auth.model

sealed class SignInResult {
    object WrongPin : SignInResult()
    object Success : SignInResult()
}