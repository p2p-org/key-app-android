package com.p2p.wallet.auth.repository

import com.p2p.wallet.auth.api.CheckCaptchaResponse
import com.p2p.wallet.auth.api.CheckUsernameResponse
import com.p2p.wallet.auth.api.LookupUsernameResponse
import com.p2p.wallet.auth.api.RegisterUsernameResponse

interface UsernameRepository {
    suspend fun checkUsername(username: String): CheckUsernameResponse
    suspend fun checkCaptcha(): CheckCaptchaResponse
    suspend fun registerUsername(username: String, result: String?): RegisterUsernameResponse
    suspend fun lookup(owner: String): ArrayList<LookupUsernameResponse>
}