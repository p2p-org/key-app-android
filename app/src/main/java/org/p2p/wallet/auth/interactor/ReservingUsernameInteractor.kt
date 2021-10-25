package org.p2p.wallet.auth.interactor

import org.p2p.wallet.auth.api.UsernameCheckResponse
import org.p2p.wallet.auth.model.NameRegisterBody
import org.p2p.wallet.auth.repository.UsernameRemoteRepository

class ReservingUsernameInteractor(
    private val usernameRemoteRepository: UsernameRemoteRepository,
) {

    suspend fun checkUsername(username: String): UsernameCheckResponse {
        return usernameRemoteRepository.checkUsername(username)
//        return usernameRemoteRepository.checkUsername("kstep-test-1")
    }

    suspend fun registerUsername(): String {
        return usernameRemoteRepository.registerUsername(
            NameRegisterBody(
                owner = "",
                credentials = NameRegisterBody.Credentials(
                    geeTestValidate = "",
                    geeTestSecCode = "",
                    geeTestChallenge = ""

                )
            )
        )
    }
}