package org.p2p.wallet.auth.repository

import org.p2p.wallet.auth.api.CheckCaptchaResponse
import org.p2p.wallet.auth.api.CheckUsernameResponse
import org.p2p.wallet.auth.api.LookupUsernameResponse
import org.p2p.wallet.auth.api.RegisterUsernameResponse

interface UsernameRepository {
    suspend fun checkUsername(username: String): CheckUsernameResponse
    suspend fun checkCaptcha(): CheckCaptchaResponse
    suspend fun registerUsername(username: String, result: String?): RegisterUsernameResponse
    suspend fun lookup(owner: String): ArrayList<LookupUsernameResponse>
}