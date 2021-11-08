package org.p2p.wallet.auth.repository

import org.p2p.wallet.auth.api.CheckCaptchaResponse
import org.p2p.wallet.auth.model.CheckUsername
import org.p2p.wallet.auth.model.LookupUsername
import org.p2p.wallet.auth.model.RegisterUsername

interface UsernameRepository {
    suspend fun checkUsername(username: String): CheckUsername
    suspend fun checkCaptcha(): CheckCaptchaResponse
    suspend fun registerUsername(username: String, result: String?): RegisterUsername
    suspend fun lookup(owner: String): LookupUsername
}