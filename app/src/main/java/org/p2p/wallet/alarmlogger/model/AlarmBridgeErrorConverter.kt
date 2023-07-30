package org.p2p.wallet.alarmlogger.model

import com.google.gson.Gson
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.Token
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.wallet.alarmlogger.api.AlarmErrorsBridgeClaimRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsRequest
import org.p2p.wallet.alarmlogger.api.AlarmErrorsSendBridgeRequest

class AlarmBridgeErrorConverter(
    private val gson: Gson,
    private val throwableFormatter: AlarmThrowableFormatter
) : AlarmFeatureConverter {

    fun toBridgeClaimErrorRequest(
        userPublicKey: Base58String,
        userEthAddress: EthAddress,
        token: Token.Eth,
        claimAmount: String,
        error: Throwable
    ): AlarmErrorsRequest {
        val tokenToClaim = AlarmErrorsBridgeClaimRequest.TokenToClaim(
            tokenName = token.tokenName,
            solanaMint = token.mintAddress.toBase58Instance(),
            ethMint = token.publicKey,
            claimAmount = claimAmount,
        )

        val request = AlarmErrorsBridgeClaimRequest(
            tokenToClaim = tokenToClaim,
            userPubkey = userPublicKey,
            userEthPubkey = userEthAddress.hex,
            simulationError = throwableFormatter.formatSimulationError(error).orEmpty(),
            feeRelayerError = throwableFormatter.formatFeeRelayerError(error).orEmpty(),
            blockchainError = throwableFormatter.formatBlockchainError(error).orEmpty()
        )

        return AlarmErrorsRequest(
            logsTitle = "Wormhole Claim Android Alarm",
            payload = gson.toJson(request)
        )
    }

    fun toBridgeSendErrorRequest(
        token: Token.Active,
        userPublicKey: Base58String,
        currency: String,
        sendAmount: String,
        arbiterFeeAmount: String,
        recipientEthPubkey: String,
        error: Throwable
    ): AlarmErrorsRequest {
        val tokenToClaim = AlarmErrorsSendBridgeRequest.TokenToSend(
            tokenName = token.tokenName,
            mint = token.mintAddress.toBase58Instance(),
            amount = sendAmount,
            currency = currency
        )
        val request = AlarmErrorsSendBridgeRequest(
            tokenToSend = tokenToClaim,
            arbiterFeeAmount = arbiterFeeAmount,
            userPubkey = userPublicKey,
            recipientEthPubkey = recipientEthPubkey,
            simulationError = throwableFormatter.formatSimulationError(error).orEmpty(),
            feeRelayerError = throwableFormatter.formatFeeRelayerError(error).orEmpty(),
            blockchainError = throwableFormatter.formatBlockchainError(error).orEmpty()
        )

        return AlarmErrorsRequest(
            logsTitle = "Wormhole Send Android Alarm",
            payload = gson.toJson(request)
        )
    }
}
