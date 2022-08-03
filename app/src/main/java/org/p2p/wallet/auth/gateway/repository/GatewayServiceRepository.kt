package org.p2p.wallet.auth.gateway.repository

import org.p2p.wallet.auth.gateway.api.response.RegisterWalletResponse

interface GatewayServiceRepository {
    suspend fun registerWalletWithSms(
        userPublicKey: String,
        userPrivateKey: String,
        etheriumPublicKey: String,
        phoneNumber: String
    ): RegisterWalletResponse

    suspend fun confirmRegisterWallet(
        userPublicKey: String,
        userPrivateKey: String,
        etheriumPublicKey: String,
        thirdShare: String,
        phoneNumber: String,
        otpConfirmationCode: String
    )
}
